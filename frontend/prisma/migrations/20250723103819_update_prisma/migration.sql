-- CreateTable
CREATE TABLE "AtsAnalysis" (
    "id" TEXT NOT NULL,
    "resumeId" TEXT NOT NULL,
    "overallScore" INTEGER NOT NULL,
    "keywordMatchScore" INTEGER NOT NULL,
    "formatScore" INTEGER NOT NULL,
    "skillsScore" INTEGER NOT NULL,
    "experienceScore" INTEGER NOT NULL,
    "matchedKeywords" TEXT[],
    "missingKeywords" TEXT[],
    "jobDescription" TEXT NOT NULL,
    "jobTitle" TEXT,
    "companyName" TEXT,
    "improvements" JSONB[],
    "suggestions" JSONB[],
    "totalKeywords" INTEGER NOT NULL,
    "matchedCount" INTEGER NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "AtsAnalysis_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "AtsAnalysis_resumeId_key" ON "AtsAnalysis"("resumeId");

-- CreateIndex
CREATE INDEX "AtsAnalysis_resumeId_idx" ON "AtsAnalysis"("resumeId");

-- AddForeignKey
ALTER TABLE "AtsAnalysis" ADD CONSTRAINT "AtsAnalysis_resumeId_fkey" FOREIGN KEY ("resumeId") REFERENCES "Resume"("id") ON DELETE CASCADE ON UPDATE CASCADE;
