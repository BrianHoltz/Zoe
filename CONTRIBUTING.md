Contributing guidelines

Please keep line endings consistent in this repository. We enforce LF (Unix) line endings using the repository's .gitattributes file.

If you're on macOS or Linux, it's recommended to set:

  git config --global core.autocrlf input

This will convert CRLF to LF on commit but leave LF untouched on checkout.

If you're on Windows, you may prefer:

  git config --global core.autocrlf true

But our .gitattributes sets eol=lf for text files; please ensure your editor respects repository settings.

If you need help, open an issue or contact the maintainers.

