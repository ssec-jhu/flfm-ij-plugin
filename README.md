# SSEC-JHU flfm_ij_plugin

[![CI](https://github.com/ssec-jhu/flfm-ij-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/ssec-jhu/flfm-ij-plugin/actions/workflows/ci.yml)
[![Documentation Status](https://readthedocs.org/projects/flfm-ij-plugin/badge/?version=latest)](https://flfm-ij-plugin.readthedocs.io/en/latest/?badge=latest)
[![codecov](https://codecov.io/gh/ssec-jhu/flfm-ij-plugin/graph/badge.svg?token=uZuEh7oKnl)](https://codecov.io/gh/ssec-jhu/flfm-ij-plugin)
[![Security](https://github.com/ssec-jhu/flfm-ij-plugin/actions/workflows/security.yml/badge.svg)](https://github.com/ssec-jhu/flfm-ij-plugin/actions/workflows/security.yml)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.14052740.svg)](https://doi.org/10.5281/zenodo.14052740)


![SSEC-JHU Logo](docs/_static/SSEC_logo_horiz_blue_1152x263.png)

This is an ImageJ 1.x plugin wrapper for the SSEC
[FLFM project](https://github.com/ssec-jhu/flfm). This project uses The
[Deep Java Library](https://djl.ai/)(djl) to load the pytorch models from the FLFM
project.

# Quickstart Guide

Get the latest `jar` file from `<release location>`. Choose the appropriate
release according to your OS. It should be one of:
 * `flfm_plugin.windows.jar`
 * `flfm_plugin.linux.jar`
 * `flfm_plugin.macos.jar` (not currently available)

Place the `jar` file into the `plugins` folder for you ImageJ installation.

Open ImageJ and select `Plugins` from the top menu, then select `FLFM Plugin`.



> [!TIP]
> ISSUES DETECTING GPU?
> Sometimes there can be an issue with the detection of the GPU when running the
> plugin. There a couple things that you can do to fix this issue:
>1. Start ImageJ with the plugin jar on the classpath:
>  - Open a terminal in the directory of the ImageJ installation
>  - Linux:
>    - `./ImageJ -cp plugins/flfm_plugin.linux.jar`
>  - Windows:
>    - `ImageJ.exe -cp plugins\flfm_plugin.windows.jar`
>2. Add the DJL cache to the `PATH` variable. The DJL library will download\extract
>   the required libraries to run the FLFM pytorch code to the DJL cache location.
>   This can be set manually by following the DJL
>   [documentation](https://docs.djl.ai/master/docs/development/cache_management.html#resource-caches)
>   The default location is something like:
> - Linux:
>   - `~/.djl.ai/pytorch/2.5.2-cu124-linux-x86_64/`
> - Windows:
>   - `C:\Users\<username>\.djl.ai\pytorch\2.5.1-cu124-win-x86_64\`

# Installation, Build, & Run instructions

### Build:

To build the project requires two steps:
1. Use the FLFM python package to export model files for all desired iterations
   (current [1-15]).
2. Build the Java plugin using Maven with the model files copied into the
   `src/main/resources/models` directory.

For convenience, the make file will do all of this for you, but you need to have
the following prerequisites installed:
* `conda`
* `maven`

  #### Docker:
  (TODO)

  #### Locally:
  1. Clone the repository:
     ```bash
     git clone https://github.com/ssec-jhu/flfm-ij-plugin.git
     ```
  2. Change into the repository directory:
     ```bash
      cd flfm-ij-plugin
      ```
  3. Run the make command:
      ```bash
      make linux
      ```
      or
      ```bash
      make windows
      ```

If you need to rebuild the project it is recommended to clean the project first:
```bash
make clean
```

### Run

  #### Docker:
  (TODO)

  #### Locally:
  (TODO)

### Usage:
(TODO)


# Testing

### Linting:
Facilitates in testing typos, syntax, style, and other simple code analysis tests.
  * ``cd`` into repo dir.
  * Use [Spotless](https://github.com/diffplug/spotless) to check code formatting:

    ```mvn spotless:check --file flfm-ij/pom.xml```
  * Use [Checkstyle](https://checkstyle.org/) for linting the code:

    ```mvn checkstyle:check --file flfm-ij/pom.xml```

### Security Checks:
Facilitates in checking for security concerns using [SpotBugs](https://spotbugs.readthedocs.io/en/stable/index.html).
 * ``cd`` into repo dir.
 * ``mvn -B spotbugs:check --file flfm-ij/pom.xml``

### Unit Tests:
Facilitates in testing core package functionality at a modular level.
  * ``cd`` into repo dir.
  * Run all available tests:

    ```mvn clean test --file flfm-ij/pom.xml```

### Regression tests:
Facilitates in testing whether core data results differ during development.
  * WIP

### Smoke Tests:
Facilitates in testing at the application and infrastructure level.
  * WIP

### Build Docs:
Facilitates in building, testing & viewing the docs.
 * ``cd`` into repo dir.
 * ``pip install -r requirements/docs.txt``
 * ``cd docs``
 * ``make clean``
 * ``make html``
 * To view the docs in your default browser run ``open docs/_build/html/index.html``.
