.PHONY: clean linux windows

MODEL_FILES := $(addprefix models/model,$(addsuffix .pt,$(shell seq 1 15)))
JAR_MODEL_FILES := $(addprefix flfm-ij/src/main/resources/models/model,$(addsuffix .pt,$(shell seq 1 15)))

# Create a python environment and install dependencies This will create a conda
# environment in the ./env directory and install the necessary packages for
# FLFM.
env/bin/python:
	@echo "Creating conda environment..."
	micromamba create -y --prefix ./env python=3.11
	micromamba run -p ./env python -m pip install git+https://github.com/ssec-jhu/flfm.git

# Export Model files using the python package
models/model%.pt: env/bin/python
	@echo "Exporting $@"
	mkdir -p models
	micromamba run -p ./env python -m flfm.cli export --out $@ --n_steps $*

# Copy the model files into the source tree
flfm-ij/src/main/resources/models/model%.pt: $(MODEL_FILES)
	@echo "Copying $< to $@"
	mkdir -p $(dir $@)
	cp $< $@

# Make the linux based JAR file
flfm-ij/target/flfm_plugin.linux.jar: $(JAR_MODEL_FILES)
	mvn package -f flfm-ij/pom.xml -P linux-cuda

# Make the windows based JAR file
flfm-ij/target/flfm_plugin.windows.jar: $(JAR_MODEL_FILES)
	mvn package -f flfm-ij/pom.xml -P windows-cuda

# convience target for the linux JAR
linux: flfm-ij/target/flfm_plugin.linux.jar

# convience target for the windows JAR
windows: flfm-ij/target/flfm_plugin.windows.jar

# clean the target directory containing previously built JAR files
clean:
	mvn clean -f flfm-ij/pom.xml
