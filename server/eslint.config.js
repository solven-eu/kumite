// https://eslint.vuejs.org/user-guide/#installation
// https://eslint.org/docs/latest/use/configure/migration-guide#--ext
import js from "@eslint/js";
import eslintPluginVue from "eslint-plugin-vue";

export default [
  js.configs.recommended,
  ...eslintPluginVue.configs["flat/recommended"],
  {
    files: ["*.js", "**/*.js", "*.vue", "**/*.vue"],
    //        plugins: {
    //            jsdoc: jsdoc
    //        },
    rules: {
      // override/add rules settings here, such as:
      // 'vue/no-unused-vars': 'error'
      //"vue/require-default-prop": "off",
    },
  },
];
