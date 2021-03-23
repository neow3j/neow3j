# Development

This file describes configurations and nuances for developing in the neow3j project.

## Locally Testing GitHub Action Workflows

If you would like to locally test GitHub Actions workflows, it's not required to make
hundreds of (useless) commits. It's better to first use [act](https://github.com/nektos/act) to
debug things.

1. Follow the [installation steps](https://github.com/nektos/act#installation).

2. [Create a PAT](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token) for your GitHub user, for reading container registries (i.e., ghcr.io).

3. Edit your local `~/.actrc` file and add the following lines in the end:

```
-s CR_PAT_USERNAME=<YOUR_GITHUB_USERNAME>
-s CR_PAT=<YOUR_GITHUB_PAT_TOKEN>
```

4. If you would like to run the `.github/workflows/intergation.yml`, run the following command:

```
act --detect-event -W .github/workflows/integration.yml
```

That's it. :rocket:
