"use client";

import { useState, useCallback } from "react";
import { useDropzone } from "react-dropzone";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Progress } from "@/components/ui/progress";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  Upload,
  FileText,
  Trash2,
  CheckCircle,
  AlertCircle,
  TrendingUp,
  Target,
  Brain,
  Award,
  Clock,
  Lightbulb,
} from "lucide-react";
import { useEffect } from "react";
import { analyzeResumeWithAI, getATSAnalysis } from "@/actions/ats-score";

export default function ATSAnalyzer() {
  const [uploadedFile, setUploadedFile] = useState(null);
  const [jobDescription, setJobDescription] = useState("");
  const [jobTitle, setJobTitle] = useState("");
  const [companyName, setCompanyName] = useState("");
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysis, setAnalysis] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  // Load existing analysis on component mount
  useEffect(() => {
    const loadExistingAnalysis = async () => {
      try {
        const existingAnalysis = await getATSAnalysis();
        setAnalysis(existingAnalysis);
      } catch (err) {
        // No existing analysis found, which is fine
        console.log("No existing analysis found");
      } finally {
        setLoading(false);
      }
    };

    loadExistingAnalysis();
  }, []);

  const onDrop = useCallback((acceptedFiles) => {
    const file = acceptedFiles[0];
    if (file) {
      const allowedTypes = [
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
      ];

      if (allowedTypes.includes(file.type)) {
        setUploadedFile(file);
        setError("");
      } else {
        setUploadedFile(null);
        setError(
          "Only PDF, DOC, or DOCX files are allowed. Please upload a valid resume file."
        );
      }
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      "application/pdf": [".pdf"],
      "application/msword": [".doc"],
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
        [".docx"],
    },
    maxFiles: 1,
    onDropRejected: () => {
      setError("Please upload a valid resume file (PDF, DOC, or DOCX)");
    },
  });

  const handleAnalyze = async () => {
    if (!uploadedFile) {
      setError("Please upload a resume file");
      return;
    }

    setIsAnalyzing(true);
    setError("");

    try {
      const result = await analyzeResumeWithAI({
        file: uploadedFile,
        jobDescription,
        jobTitle,
        companyName,
      });

      setAnalysis(result);
    } catch (err) {
      setError(err.message || "Failed to analyze resume. Please try again.");
      console.error("Analysis error:", err);
    } finally {
      setIsAnalyzing(false);
    }
  };

  const removeFile = () => {
    setUploadedFile(null);
    setError("");
  };

  const getScoreColor = (score) => {
    if (score >= 80) return "text-green-600";
    if (score >= 60) return "text-yellow-600";
    return "text-red-600";
  };

  const getScoreBgColor = (score) => {
    if (score >= 80) return "bg-green-100";
    if (score >= 60) return "bg-yellow-100";
    return "bg-red-100";
  };

  if (loading) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* Upload Section */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Upload className="h-5 w-5" />
            Upload Resume
          </CardTitle>
          <CardDescription>
            Upload your resume in PDF, DOC, or DOCX format for ATS analysis.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {!uploadedFile ? (
            <div
              {...getRootProps()}
              className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors ${
                isDragActive
                  ? "border-blue-500 bg-blue-50"
                  : "border-gray-300 hover:border-gray-400"
              }`}
            >
              <input {...getInputProps()} />
              <Upload className="h-12 w-12 mx-auto mb-4 text-gray-400" />
              <p className="text-lg mb-2">
                {isDragActive
                  ? "Drop your resume here"
                  : "Drop your resume here or click to browse"}
              </p>
              <p className="text-sm text-gray-500">
                Supports PDF, DOC, and DOCX files only
              </p>
            </div>
          ) : (
            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
              <div className="flex items-center gap-3">
                <FileText className="h-6 w-6 text-blue-600" />
                <div>
                  <p className="font-medium">{uploadedFile.name}</p>
                  <p className="text-sm text-gray-500">
                    {(uploadedFile.size / 1024).toFixed(1)} KB
                  </p>
                  <p className="text-xs text-green-600">
                    Ready for analysis
                  </p>
                </div>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={removeFile}
                className="text-red-600 hover:text-red-700"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Job Information Section */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Target className="h-5 w-5" />
            Job Information (Optional)
          </CardTitle>
          <CardDescription>
            Provide job details for more accurate ATS analysis
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="job-title">Job Title</Label>
              <Input
                id="job-title"
                placeholder="e.g., Software Engineer"
                value={jobTitle}
                onChange={(e) => setJobTitle(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="company-name">Company Name</Label>
              <Input
                id="company-name"
                placeholder="e.g., Google"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
              />
            </div>
          </div>
          <div className="space-y-2">
            <Label htmlFor="job-description">Job Description</Label>
            <Textarea
              id="job-description"
              placeholder="Paste the job description here for better keyword matching..."
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              rows={6}
            />
          </div>
        </CardContent>
      </Card>

      {/* Analyze Button */}
      <div className="flex justify-center">
        <Button
          onClick={handleAnalyze}
          disabled={isAnalyzing || !uploadedFile}
          className="px-8 py-3 text-lg"
        >
          {isAnalyzing ? (
            <>
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              Analyzing Resume...
            </>
          ) : (
            <>
              <Brain className="h-5 w-5 mr-2" />
              Analyze Resume
            </>
          )}
        </Button>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Analysis Results */}
      {analysis && (
        <div className="space-y-6">
          <Separator />

          {/* Overall Score */}
          <Card>
            <CardHeader className="text-center">
              <CardTitle className="text-2xl">ATS Analysis Results</CardTitle>
              <div
                className={`text-6xl font-bold ${getScoreColor(
                  analysis.overallScore
                )}`}
              >
                {analysis.overallScore}
              </div>
              <p className="text-gray-600">Overall ATS Score</p>
            </CardHeader>
          </Card>

          {/* Score Breakdown */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {[
              {
                label: "Keywords",
                score: analysis.keywordMatchScore,
                icon: Target,
              },
              { label: "Format", score: analysis.formatScore, icon: FileText },
              { label: "Skills", score: analysis.skillsScore, icon: Award },
              {
                label: "Experience",
                score: analysis.experienceScore,
                icon: Clock,
              },
            ].map((item, index) => (
              <Card key={index}>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <item.icon className="h-4 w-4 text-gray-600" />
                      <span className="text-sm font-medium">{item.label}</span>
                    </div>
                    <span className={`font-bold ${getScoreColor(item.score)}`}>
                      {item.score}%
                    </span>
                  </div>
                  <Progress value={item.score} className="h-2" />
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Keyword Analysis */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-green-600">
                  <CheckCircle className="h-5 w-5" />
                  Matched Keywords ({analysis.matchedCount})
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {analysis.matchedKeywords.map((keyword, index) => (
                    <Badge
                      key={index}
                      variant="secondary"
                      className="bg-green-100 text-green-800"
                    >
                      {keyword}
                    </Badge>
                  ))}
                </div>
                {analysis.matchedKeywords.length === 0 && (
                  <p className="text-gray-500 text-sm">
                    No matched keywords found
                  </p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-red-600">
                  <AlertCircle className="h-5 w-5" />
                  Missing Keywords
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {analysis.missingKeywords.map((keyword, index) => (
                    <Badge
                      key={index}
                      variant="secondary"
                      className="bg-red-100 text-red-800"
                    >
                      {keyword}
                    </Badge>
                  ))}
                </div>
                {analysis.missingKeywords.length === 0 && (
                  <p className="text-gray-500 text-sm">
                    No missing keywords identified
                  </p>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Improvements */}
          {analysis.improvements && analysis.improvements.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5" />
                  Recommendations for Improvement
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {analysis.improvements.map((improvement, index) => (
                    <div
                      key={index}
                      className="border-l-4 border-l-blue-500 pl-4"
                    >
                      <div className="flex items-start justify-between mb-2">
                        <h4 className="font-semibold">
                          {improvement.category}
                        </h4>
                        <Badge
                          variant={
                            improvement.priority === "high"
                              ? "destructive"
                              : "secondary"
                          }
                        >
                          {improvement.priority}
                        </Badge>
                      </div>
                      <p className="text-gray-600 mb-2">{improvement.issue}</p>
                      <p className="text-sm text-gray-800 mb-2">
                        {improvement.suggestion}
                      </p>
                      {improvement.keywords &&
                        improvement.keywords.length > 0 && (
                          <div className="flex flex-wrap gap-1">
                            {improvement.keywords.map((keyword, kIndex) => (
                              <Badge
                                key={kIndex}
                                variant="outline"
                                className="text-xs"
                              >
                                {keyword}
                              </Badge>
                            ))}
                          </div>
                        )}
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Suggestions */}
          {analysis.suggestions && analysis.suggestions.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Lightbulb className="h-5 w-5" />
                  Additional Suggestions
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {analysis.suggestions.map((suggestion, index) => (
                    <div
                      key={index}
                      className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg"
                    >
                      <div
                        className={`p-1 rounded-full ${
                          suggestion.impact === "high"
                            ? "bg-red-100"
                            : "bg-yellow-100"
                        }`}
                      >
                        <Lightbulb
                          className={`h-3 w-3 ${
                            suggestion.impact === "high"
                              ? "text-red-600"
                              : "text-yellow-600"
                          }`}
                        />
                      </div>
                      <div className="flex-1">
                        <h4 className="font-medium mb-1">{suggestion.title}</h4>
                        <p className="text-sm text-gray-600">
                          {suggestion.description}
                        </p>
                      </div>
                      <Badge
                        variant={
                          suggestion.impact === "high"
                            ? "destructive"
                            : "secondary"
                        }
                      >
                        {suggestion.impact} impact
                      </Badge>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      )}
    </div>
  );
}