"use client";
import { SignIn } from "@clerk/clerk-react";

const Page = () => {
  return (
    <div className="min-h-screen flex items-center justify-centersm:px-6">
      <div className="w-full max-w-md">
        <SignIn />
      </div>
    </div>
  );
};

export default Page;
