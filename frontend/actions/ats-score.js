"use server";

import { db } from "@/lib/prisma";
import { auth } from "@clerk/nextjs/server";
import { GoogleGenerativeAI } from "@google/generative-ai";
import { revalidatePath } from "next/cache";
import PDFParser from "pdf2json";
import mammoth from "mammoth";

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);
const model = genAI.getGenerativeModel({
  model: "gemini-2.0-flash",
});

// Analyze resume with AI and store results in database
export async function analyzeResumeWithAI({
  file,
  jobDescription,
  jobTitle,
  companyName,
}) {
  const { userId } = await auth();
  if (!userId) throw new Error("Unauthorized");

  const user = await db.user.findUnique({
    where: { clerkUserId: userId },
  });

  if (!user) throw new Error("User not found");

  // Validate file type
  const allowedTypes = [
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  ];

  if (!allowedTypes.includes(file.type)) {
    throw new Error(
      "Invalid file type. Only PDF, DOC, and DOCX files are supported."
    );
  }

  // Extract text content from file
  let resumeContent;

  try {
    resumeContent = await extractTextFromFile(file);
  } catch (error) {
    console.error("File extraction error:", error);
    throw new Error(
      "Failed to extract text from the file. Please ensure the file is not corrupted and try again."
    );
  }

  // Clean and validate resume content
  const cleanedContent = cleanResumeContent(resumeContent);
  if (!cleanedContent) {
    throw new Error(
      "Unable to extract readable text from the file. Please ensure the document contains text content."
    );
  }

  // Upsert resume
  const resume = await db.resume.upsert({
    where: { userId: user.id },
    update: { content: cleanedContent },
    create: { userId: user.id, content: cleanedContent },
  });

  // Perform analysis using Gemini AI
  const analysis = await performATSAnalysis(
    cleanedContent,
    jobDescription,
    jobTitle
  );

  // Upsert ATS analysis
  const atsAnalysis = await db.atsAnalysis.upsert({
    where: { resumeId: resume.id },
    update: {
      ...analysis,
      jobDescription: jobDescription || "No job description provided",
      jobTitle: jobTitle || null,
      companyName: companyName || null,
    },
    create: {
      resumeId: resume.id,
      ...analysis,
      jobDescription: jobDescription || "No job description provided",
      jobTitle: jobTitle || null,
      companyName: companyName || null,
    },
  });

  revalidatePath("/ats");
  return atsAnalysis;
}

// Extract text content from PDF, DOC, or DOCX files using pdf2json
async function extractTextFromFile(file) {
  const buffer = Buffer.from(await file.arrayBuffer());

  switch (file.type) {
    case "application/pdf":
      try {
        return await extractPDFText(buffer);
      } catch (error) {
        console.error("PDF parsing error:", error);
        throw new Error("Failed to extract text from PDF file");
      }

    case "application/msword":
      throw new Error(
        "DOC files are not fully supported yet. Please convert to DOCX format."
      );

    case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
      try {
        const result = await mammoth.extractRawText({ buffer });
        return result.value;
      } catch (error) {
        console.error("DOCX parsing error:", error);
        throw new Error("Failed to extract text from DOCX file");
      }

    default:
      throw new Error("Unsupported file type");
  }
}

// Extract text from PDF using pdf2json
function extractPDFText(buffer) {
  return new Promise((resolve, reject) => {
    const pdfParser = new PDFParser(null, 1);

    pdfParser.on("pdfParser_dataError", (errData) => {
      console.error("PDF Parser Error:", errData.parserError);
      reject(new Error(`PDF parsing failed: ${errData.parserError}`));
    });

    pdfParser.on("pdfParser_dataReady", (pdfData) => {
      try {
        // Extract text from all pages
        let extractedText = "";

        if (pdfData && pdfData.Pages && Array.isArray(pdfData.Pages)) {
          pdfData.Pages.forEach((page) => {
            if (page.Texts && Array.isArray(page.Texts)) {
              page.Texts.forEach((textObj) => {
                if (textObj.R && Array.isArray(textObj.R)) {
                  textObj.R.forEach((textRun) => {
                    if (textRun.T) {
                      // Decode URI component to handle special characters
                      const decodedText = decodeURIComponent(textRun.T);
                      extractedText += decodedText + " ";
                    }
                  });
                }
              });
              extractedText += "\n"; 
            }
          });
        }

        if (!extractedText.trim()) {
          reject(new Error("No text content found in PDF"));
          return;
        }

        resolve(extractedText.trim());
      } catch (parseError) {
        console.error("Text extraction error:", parseError);
        reject(new Error("Failed to extract text from parsed PDF data"));
      }
    });

    // Parse the PDF buffer
    try {
      pdfParser.parseBuffer(buffer);
    } catch (error) {
      console.error("Buffer parsing error:", error);
      reject(new Error("Failed to parse PDF buffer"));
    }
  });
}

// Clean resume content valid UTF-8 text
function cleanResumeContent(content) {
  if (!content || typeof content !== "string") {
    return null;
  }

  let cleaned = content
    .replace(/\x00/g, "") 
    .replace(/[\x01-\x08\x0B\x0C\x0E-\x1F\x7F]/g, "")
    .replace(/\s+/g, " ") 
    .trim();

  if (cleaned.length < 50) {
    return null;
  }

  const printableChars = cleaned.match(/[a-zA-Z0-9\s\.\,\!\?\-\(\)\[\]\{\}]/g);
  const printableRatio = printableChars
    ? printableChars.length / cleaned.length
    : 0;

  // If less than 50% printable characters, it's likely corrupted data
  if (printableRatio < 0.5) {
    return null;
  }

  return cleaned;
}

// Fetch existing ATS analysis for the authenticated user
export async function getATSAnalysis() {
  const { userId } = await auth();
  if (!userId) throw new Error("Unauthorized");

  const user = await db.user.findUnique({
    where: { clerkUserId: userId },
    include: {
      resume: {
        include: { atsAnalysis: true },
      },
    },
  });

  if (!user?.resume?.atsAnalysis) {
    throw new Error("No ATS analysis found");
  }

  return user.resume.atsAnalysis;
}

// Delete existing resume and analysis
export async function deleteResumeAnalysis() {
  const { userId } = await auth();
  if (!userId) throw new Error("Unauthorized");

  const user = await db.user.findUnique({
    where: { clerkUserId: userId },
    include: { resume: true },
  });

  if (!user?.resume) {
    throw new Error("No resume found to delete");
  }

  // Delete resume (this will cascade delete the ATS analysis)
  await db.resume.delete({
    where: { id: user.resume.id },
  });

  revalidatePath("/ats");
  return { success: true };
}

//Get Resume Content
export async function getResumeContent() {
  const { userId } = await auth();
  if (!userId) throw new Error("Unauthorized");

  const user = await db.user.findUnique({
    where: { clerkUserId: userId },
    include: { resume: true },
  });

  return user?.resume?.content || null;
}

// function to perform AI analysis using Gemini
async function performATSAnalysis(resumeContent, jobDescription, jobTitle) {
  const prompt = `
    Analyze this resume against the job description for ATS compatibility and provide a detailed scoring breakdown.

    RESUME CONTENT:
    ${resumeContent}

    JOB DESCRIPTION:
    ${
      jobDescription ||
      "No specific job description provided - perform general ATS analysis"
    }

    JOB TITLE: ${jobTitle || "Not specified"}

    Please analyze and return a JSON response with the following structure:
    {
      "overallScore": number (0-100),
      "keywordMatchScore": number (0-100),
      "formatScore": number (0-100),
      "skillsScore": number (0-100),
      "experienceScore": number (0-100),
      "matchedKeywords": ["keyword1", "keyword2"],
      "missingKeywords": ["keyword1", "keyword2"],
      "totalKeywords": number,
      "matchedCount": number,
      "improvements": [
        {
          "category": "Keywords",
          "issue": "Missing important keywords",
          "suggestion": "Add these keywords naturally to your resume",
          "keywords": ["keyword1", "keyword2"],
          "priority": "high"
        }
      ],
      "suggestions": [
        {
          "title": "Improve Keyword Density",
          "description": "Add more relevant keywords from the job description",
          "impact": "high"
        }
      ]
    }

    Analysis Guidelines:
    - overallScore should be calculated based on all other scores
    - keywordMatchScore should compare resume keywords with job description keywords
    - formatScore should evaluate ATS-friendly formatting (simple structure, standard sections, etc.)
    - skillsScore should assess technical and soft skills mentioned
    - experienceScore should evaluate relevant work experience
    - Provide specific, actionable suggestions
    - If no job description is provided, focus on general ATS best practices
    - Keywords should be relevant to the industry and role
    - Improvements should be categorized and prioritized
    - Be thorough and provide specific, actionable suggestions
    - Consider that this resume was extracted from a document file, so formatting may be simplified
  `;

  try {
    const result = await model.generateContent(prompt);
    const response = result.response;
    const text = response.text();

    // Extract JSON from the response
    const jsonMatch = text.match(/\{[\s\S]*\}/);
    if (!jsonMatch) {
      console.error("No JSON found in Gemini response:", text);
      throw new Error("Invalid response format from AI");
    }

    const data = JSON.parse(jsonMatch[0]);

    // Validate and sanitize the response
    return {
      overallScore: Math.min(100, Math.max(0, data.overallScore || 0)),
      keywordMatchScore: Math.min(
        100,
        Math.max(0, data.keywordMatchScore || 0)
      ),
      formatScore: Math.min(100, Math.max(0, data.formatScore || 0)),
      skillsScore: Math.min(100, Math.max(0, data.skillsScore || 0)),
      experienceScore: Math.min(100, Math.max(0, data.experienceScore || 0)),
      matchedKeywords: Array.isArray(data.matchedKeywords)
        ? data.matchedKeywords
        : [],
      missingKeywords: Array.isArray(data.missingKeywords)
        ? data.missingKeywords
        : [],
      totalKeywords: data.totalKeywords || 0,
      matchedCount: data.matchedCount || 0,
      improvements: Array.isArray(data.improvements) ? data.improvements : [],
      suggestions: Array.isArray(data.suggestions) ? data.suggestions : [],
    };
  } catch (error) {
    console.error("Gemini Analysis Error:", error);

    // Get user profile data for enhanced fallback analysis
    const { userId } = await auth();
    let userProfile = null;

    try {
      if (userId) {
        const user = await db.user.findUnique({
          where: { clerkUserId: userId },
          include: {
            profile: true,
          },
        });
        userProfile = user?.profile;
      }
    } catch (profileError) {
      console.error("Error fetching user profile:", profileError);
    }

    // Enhanced fallback analysis with Gemini API for keywords
    const fallbackKeywords = await extractBasicKeywords(
      resumeContent,
      jobDescription,
      userProfile
    );

    return {
      overallScore: 65,
      keywordMatchScore: 60,
      formatScore: 75,
      skillsScore: 60,
      experienceScore: 65,
      matchedKeywords: fallbackKeywords.matched,
      missingKeywords: fallbackKeywords.missing,
      totalKeywords: fallbackKeywords.total,
      matchedCount: fallbackKeywords.matched.length,
      improvements: [
        {
          category: "Analysis Error",
          issue: "AI analysis temporarily unavailable",
          suggestion: "Please try again later for detailed analysis",
          keywords: [],
          priority: "medium",
        },
      ],
      suggestions: [
        {
          title: "Retry Analysis",
          description:
            "The AI analysis encountered an error. Please try uploading your resume again.",
          impact: "high",
        },
      ],
    };
  }
}

// Extract basic keywords using Gemini AI
async function extractBasicKeywords(
  resumeContent,
  jobDescription,
  userProfile = null
) {
  try {
    const keywordExtractionPrompt = `
      Analyze the following resume content and extract relevant technical and soft skills keywords for ATS optimization.
      
      RESUME CONTENT:
      ${resumeContent}
      
      JOB DESCRIPTION:
      ${jobDescription || "No specific job description provided"}
      
      USER PROFILE DATA:
      ${
        userProfile
          ? JSON.stringify(userProfile)
          : "No user profile data available"
      }
      
      Please provide a JSON response with the following structure:
      {
        "technicalSkills": ["skill1", "skill2", "skill3"],
        "softSkills": ["skill1", "skill2", "skill3"],
        "industryKeywords": ["keyword1", "keyword2", "keyword3"],
        "matchedKeywords": ["keywords found in resume"],
        "suggestedKeywords": ["keywords that should be added based on job description and profile"]
      }
      
      Guidelines:
      - Extract actual technical skills mentioned in the resume
      - Identify soft skills demonstrated through experience descriptions
      - Include industry-specific keywords relevant to the job/profile
      - Suggest keywords from job description that are missing from resume
      - Limit each category to maximum 15 items
      - Be specific and relevant to the user's field/industry
      - Consider the user's profile data (experience level, field, etc.) if available
    `;

    const result = await model.generateContent(keywordExtractionPrompt);
    const response = result.response;
    const text = response.text();

    // Extract JSON from the response
    const jsonMatch = text.match(/\{[\s\S]*\}/);
    if (jsonMatch) {
      const aiKeywords = JSON.parse(jsonMatch[0]);

      const allAIKeywords = [
        ...(aiKeywords.technicalSkills || []),
        ...(aiKeywords.softSkills || []),
        ...(aiKeywords.industryKeywords || []),
      ];

      const resumeLower = resumeContent.toLowerCase();
      const jobDescLower = (jobDescription || "").toLowerCase();

      const matched = allAIKeywords.filter((keyword) =>
        resumeLower.includes(keyword.toLowerCase())
      );

      const missing = [
        ...(aiKeywords.suggestedKeywords || []),
        ...allAIKeywords.filter(
          (keyword) =>
            jobDescLower.includes(keyword.toLowerCase()) &&
            !resumeLower.includes(keyword.toLowerCase())
        ),
      ].slice(0, 15); 

      return {
        matched: matched.slice(0, 20), 
        missing: [...new Set(missing)], 
        total: matched.length + missing.length,
      };
    }
  } catch (aiError) {
    console.error("Gemini keyword extraction error:", aiError);
  }
  s;

  return {
    matched: [],
    missing: [],
    total: 0,
  };
}
