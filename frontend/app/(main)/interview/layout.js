import { Suspense } from "react";
import { BarLoader } from "react-spinners";

export default function Layout({ children }) {
  return (
    <div className="px-2 sm:px-4 lg:px-5 w-full min-h-screen">
      <Suspense
        fallback={
          <div className="w-full flex justify-center py-8">
            <BarLoader className="mt-4" width={"100%"} color="gray" />
          </div>
        }
      >
        {children}
      </Suspense>
    </div>
  );
}