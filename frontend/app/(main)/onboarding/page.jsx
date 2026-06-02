import { getUserOnboardingStatus } from "@/actions/user";
import { industries } from "@/data/indutries";
import { redirect } from "next/navigation";
import React from "react";
import OnBoardingForm from "./_components/onboarding-page-form";

const OnboardingPage = async () => {
  const { isOnboarded } = await getUserOnboardingStatus();
  if (isOnboarded) {
    redirect("/dashboard");
  }
  return (
    <main>
      <OnBoardingForm industries={industries} />
    </main>
  );
};

export default OnboardingPage;
