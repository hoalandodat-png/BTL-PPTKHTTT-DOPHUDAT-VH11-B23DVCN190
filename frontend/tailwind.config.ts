import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        ptit: {
          red: "#B91C1C",
          "red-dark": "#991B1B",
          "red-light": "#FEF2F2",
          navy: "#0F172A",
          slate: "#1E293B",
        },
      },
    },
  },
  plugins: [],
};

export default config;
