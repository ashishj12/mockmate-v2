<h1 align="center">💼 MockMate – AI Career Coach 🤖</h1>

![Demo App](https://github.com/ashishj12/mockmate/blob/main/mockmate.png)

# Overview

MockMate is an AI-powered career coaching platform designed to help professionals advance their careers through intelligent automation. Built with Next.js 14 and integrated with Google's Gemini AI, the application offers a comprehensive suite of tools including an ATS-optimized resume builder, personalized cover letter generator, interactive interview preparation system, and real-time industry insights dashboard.

## Features

- **Smart Resume Builder**: Generate ATS-optimized resumes tailored to specific job descriptions.
- **Dynamic Cover Letter Generator**: Create personalized cover letters.
- **Interview Preparation**: Practice with AI-generated questions and receive feedback.
- **Industry Insights**: Real-time market trends and salary insights.
- **User Authentication**: Secure sign-in/sign-up using Clerk.
- **Responsive Design**: Seamless experience across devices.

## Architecture Overview

    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
    │   Frontend      │    │   Backend API   │    │   AI Services   │
    │   (Next.js)     │◄──►│   (Next.js)     │◄──►│   (Gemini AI)   │
    └─────────────────┘    └─────────────────┘    └─────────────────┘
            │                       │                       │
            ▼                       ▼                       ▼
    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
    │   Auth Layer    │    │   Database      │    │   Background    │
    │   (Clerk)       │    │   (Neon PG)     │    │   Jobs (Inngest)│
    └─────────────────┘    └─────────────────┘    └─────────────────┘

## Installation & Setup

### Prerequisites

- Node.js 18.17 or later
- npm, yarn, or pnpm
- PostgreSQL database (local or cloud)

1. Clone the repository

    ```shell
        git clone https://github.com/ashishj12/mockmate.git
        cd mockmate
    ```

2. Create a `.env` file with the following content:

   ```

       NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=
       CLERK_SECRET_KEY=

       NEXT_PUBLIC_CLERK_SIGN_IN_URL=/sign-in
       NEXT_PUBLIC_CLERK_SIGN_UP_URL=/sign-up
       NEXT_PUBLIC_CLERK_AFTER_SIGN_IN_URL=/onboarding
       NEXT_PUBLIC_CLERK_AFTER_SIGN_IN_URL=/onboarding

       DATABASE_URL=
       GEMINI_API_KEY=

   ```

3. DataBase Setup

   ```shell

        # database setup
        npx prisma migrate dev --name init
        npx prisma generate

   ```

5. For Inngest (Background Jobs)

   ```shell
        npm install -g inngest-cli
        npx inngest-cli@latest dev
   ```

6. Install dependencies:

   ```shell
        npm install
    ```


7. Run the development server:

   ```shell
        npm run dev
   ```

8. Open [http://localhost:3000](http://localhost:3000) in your browser

## Docker Run

   ```shell
        docker compose up --build
   ```

## Deployment

This application can be easily deployed to Vercel:

```shell
    npm run build
    npm run start
```

Or connect your GitHub repository to Vercel for automatic deployments.

## Technologies Used

- **Next.js & Vite**: Fast, modern frameworks for frontend and full-stack development
- **Tailwind CSS & Shadcn UI**: Utility-first styling and pre-built UI components
- **Clerk**: Secure authentication and user management system
- **Neon**: Scalable PostgreSQL database solution with serverless support
- **Inngest**: Run Background Jobs
- **Gemini AI**: Google’s LLM for generating smart, context-aware career guidance
- **Vercel**: Deployment platform ensuring high performance and global scalability

## Learn More

To learn more about the technologies used in this project:

- [Next.js Documentation](https://nextjs.org/docs)
- [Clerk Documentation](https://clerk.com/docs)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Shadcn UI Documentation](https://ui.shadcn.com/docs)
- [Gemini API Documentation](https://ai.google.dev/gemini-api)
- [Inngest Documentation](https://www.inngest.com/docs)
- [Neon Documentation](https://neon.tech/docs)

## Connect with Me

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/ashishj12)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/ashish-kumar86j)
