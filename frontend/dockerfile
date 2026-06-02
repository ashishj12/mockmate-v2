# 1. Build stage
FROM node:18-alpine AS builder

WORKDIR /app

# Copy and install dependencies
COPY package*.json ./
RUN npm install

# Copy entire project
COPY . .

# Define build-time arguments
ARG NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY
ARG CLERK_PUBLISHABLE_KEY

# Expose them as environment variables for the build
ENV NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=${NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY}
ENV CLERK_PUBLISHABLE_KEY=${CLERK_PUBLISHABLE_KEY}

# Generate Prisma client and build the app
RUN npx prisma generate
RUN npm run build

# 2. Runtime stage
FROM node:18-alpine

WORKDIR /app

# Copy only production dependencies
COPY package*.json ./
RUN npm install --omit=dev

# Copy built app and required files from builder
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/prisma ./prisma
COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma

# Copy .env files
COPY .env* ./

# Set environment for production
ENV NODE_ENV=production

# Ensure DB is ready before app starts
CMD ["sh", "-c", "npx prisma migrate deploy && npm start"]

EXPOSE 3000
