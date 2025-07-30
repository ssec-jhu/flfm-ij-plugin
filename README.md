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

**ISSUES DETECTING GPU?**

Sometimes there can be an issue with the detection of the GPU when running the
plugin. There a couple things that you can do to fix this issue:

1. Start ImageJ with the plugin jar on the classpath:
  - Open a terminal in the directory of the ImageJ installation
  - Linux:
    - `./ImageJ -cp plugins/flfm_plugin.linux.jar`
  - Windows:
    - `ImageJ.exe plugins\flfm_plugin.windows.jar`
2. Add the DJL cache to the `PATH` variable.
 - Linux
 - Windows


# Installation, Build, & Run instructions

IJ notes:
- You need to start IJ with the classpath to the plugin jar
`./ImageJ -cp plugins/flfm_plugin.linux.jar`
- You need to add the djl cache to the path if it can't find your GPU
This can be usually found at `~/.djl.ai/pytorch/2.5.2-cu124-linux-x86_64/`
or `C:\Users\<username>\.djl.ai\pytorch\2.5.1-cu124-win-x86_64\`

### Conda:

For additional cmds see the [Conda cheat-sheet](https://docs.conda.io/projects/conda/en/4.6.0/_downloads/52a95608c49671267e40c689e0bc00ca/conda-cheatsheet.pdf).

 * Download and install either [miniconda](https://docs.conda.io/en/latest/miniconda.html#installing) or [anaconda](https://docs.anaconda.com/free/anaconda/install/index.html).
 * Create new environment (env) and install ``conda create -n <environment_name>``
 * Activate/switch to new env ``conda activate <environment_name>``
 * ``cd`` into repo dir.
 * Install ``python`` and ``pip`` ``conda install python=3.11 pip``
 * Install all required dependencies (assuming local dev work), there are two ways to do this
   * If working with tox (recommended) ``pip install -r requirements/dev.txt``.
   * If you would like to setup an environment with all requirements to run outside of tox ``pip install -r requirements/all.txt``.

### Build:

  #### with Docker:
  * Download & install Docker - see [Docker install docs](https://docs.docker.com/get-docker/).
  * ``cd`` into repo dir.
  * Build image: ``docker build -t <image_name> .``

  #### with Python ecosystem:
  * ``cd`` into repo dir.
  * ``conda activate <environment_name>``
  * Build and install package in <environment_name> conda env: ``pip install .``
  * Do the same but in dev/editable mode (changes to repo will be reflected in env installation upon python kernel restart)
    _NOTE: This is the preferred installation method for dev work._
    ``pip install -e .``.
    _NOTE: If you didn't install dependencies from ``requirements/dev.txt``, you can install
    a looser constrained set of deps using: ``pip install -e .[dev]``._

### Run

  #### with Docker:
  * Follow the above [Build with Docker instructions](#with-docker).
  * Run container from image: ``docker run -d -p 8000:8000 <image_name>``. _NOTE: ``-p 8000:8000`` is specific to the example application using port 8000._
  * Alternatively, images can be pulled from ``ghcr.io/ssec-jhu/`` e.g., ``docker pull ghcr.io/flfm-ij-plugin:pr-1``.

  #### with Python ecosystem:
  * Follow the above [Build with Python ecosystem instructions](#with-python-ecosystem).
  * Run ``uvicorn flfm_ij_plugin.app.main:app --host 0.0.0.0 --port", "8000``. _NOTE: This is just an example and is obviously application dependent._

### Usage:
To be completed by child repo.


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
