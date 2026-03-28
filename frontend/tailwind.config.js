/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eff8ff',
          100: '#dbeefe',
          200: '#bfe3fe',
          300: '#93d2fd',
          400: '#60b8fa',
          500: '#3b99f5',
          600: '#257bea',
          700: '#1d64d7',
          800: '#1e51ae',
          900: '#1e4689',
        },
        wanderlust: {
          primary: '#1d64d7',
          secondary: '#f59e0b',
          accent: '#10b981',
        },
      },
    },
  },
  plugins: [],
};
