"use client";
import { SignUp } from "@clerk/nextjs";
import React from "react";

const Page = () => {
  return (
    <div className="min-h-screen flex items-center justify-center">
      <SignUp />
    </div>
  );
};

export default Page;
