.PHONY: all clean clean-models flfm-py linux models windows
.DEFAULT_GOAL: linux

MODEL_FILES := $(addprefix flfm-ij/src/main/resources/models/model,$(addsuffix .pt,$(shell seq 1 15)))

all: clean-models linux windows

help:
	@echo "Makefile targets:"
	@echo "all -> Regenerate the model files and build the linux and windows jar files"
	@echo "flfm-py -> installs the the flfm python package into a python env in the build directory"
	@echo "models -> remove any existing models and generate them again"
	@echo "windows -> build the windows jar file for distribution"
	@echo "linux -> build the linux jar file for distribution"
	@echo "clean -> remove all model files and contents of build directory"

# Create a python environment and install dependencies This will create a conda
# environment in the ./env directory and install the necessary packages for
# FLFM.
build/env/bin/python:
	@echo "Creating conda environment..."
	conda create -y --prefix build/env python=3.11

# Install the python FLFM package to generate the model files used in the jar file
flfm-py: build/env/bin/python
	@echo "Installing the FLFM python package"
	conda run -p build/env python -m pip install --force-reinstall git+https://github.com/ssec-jhu/flfm.git

# Build the model files and place them in the models dir in the source treee
flfm-ij/src/main/resources/models/model%.pt: flfm-py
	@echo "Exporting $@"
	mkdir -p models
	conda run -p build/env python -m flfm.cli export --out $@ --n_steps $*

# remove the current model files and then generate them again
models: clean-models $(MODEL_FILES)

# generate the windows jar file
windows: $(MODEL_FILES)
	mvn package -f flfm-ij/pom.xml -P windows-cuda

# Generate the linux jar file
linux: $(MODEL_FILES)
	mvn package -f flfm-ij/pom.xml -P linux-cuda

# remove model files
clean-models:
	@echo "Removing model files"
	@for file in $(MODEL_FILES); do \
		if [ -e "$$file" ]; then \
			rm "$$file"; \
		fi \
	done

# remove all build files
clean: clean-models
	mvn clean -f flfm-ij/pom.xml
	if [ -e build/env ]; then \
		conda env remove -y -p build/env; \
	fi
	rm -rf ./build
