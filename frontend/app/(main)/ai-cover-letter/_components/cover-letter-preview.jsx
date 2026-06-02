"use client";

import React from "react";
import MDEditor from "@uiw/react-md-editor";

const CoverLetterPreview = ({ content }) => {
  return (
    <div className="overflow-x-auto rounded-md border border-muted bg-background p-4 shadow-sm">
      <MDEditor
        value={content}
        preview="preview"
        height={700}
        hideToolbar
      />
    </div>
  );
};

export default CoverLetterPreview;
