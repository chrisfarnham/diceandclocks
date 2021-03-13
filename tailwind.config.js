module.exports = {
  purge: [
    './resources/public/**/*.html',
    './resources/public/**/*.js',
  ],
  darkMode: false, // or 'media' or 'class'
  theme: {
    extend: {
      maxHeight: (theme) => ({
        ...theme("spacing"),
        full: "100%", // You can add additional custom options below it too
        screen: "100vh",
      }),
      spacing: {
        72: "18rem",
        80: "20rem",
        88: "22rem",
        98: "24.5rem",
        108: "27rem",
        118: "29.5rem",
        128: "32rem",
        138: "34.5rem",
        150: "37.5rem",
        162: "40.5rem",
        174: "43.5rem",
        186: "46.5rem",
        198: "49.5rem",
        214: "53.5rem",
        228: "57rem",
        242: "60.5rem",
        256: "64rem",
        272: "68rem",
        288: "72rem",
        314: "76rem",
        330: "80rem",
      },
      screens: {
        'print': {'raw': 'print'},
      }
    },
  },
  corePlugins: {
  },
  variants: {
    extend: {
      
    },
  },
  plugins: [
    require('@tailwindcss/typography'),
  ],
}
