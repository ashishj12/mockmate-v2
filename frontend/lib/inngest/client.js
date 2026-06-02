import { Inngest } from "inngest";

//create a client to send or recieve events
export const inngest = new Inngest({
  id: "mockmate",
  name: "MockMate",
  credentials: {
    gemini: {
      apiKey: process.env.GEMINI_API_KEY,
    },
  },
});
