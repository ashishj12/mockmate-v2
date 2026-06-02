import { Suspense } from "react";
import { Card, CardContent } from "@/components/ui/card";
import ATSAnalyzer from "./_components/ATSAnalyzer";

export default function ATSPage() {
  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      {/* Header Section */}
      <div className="text-center mb-8">
        <h1 className="text-4xl font-bold mb-4 gradient-title ">
          ATS Resume Analyzer
        </h1>
        <p className="text-muted-foreground text-lg max-w-2xl mx-auto">
          Optimize your resume for Applicant Tracking Systems and increase your
          chances of landing interviews
        </p>
      </div>

      {/* Main ATS Analyzer Component */}
      <Suspense
        fallback={
          <Card>
            <CardContent className="flex items-center justify-center h-64">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </CardContent>
          </Card>
        }
      >
        <ATSAnalyzer />
      </Suspense>
    </div>
  );
}
