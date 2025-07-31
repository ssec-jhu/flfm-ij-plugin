.PHONY: clean linux windows

# Create a python environment and install dependencies This will create a conda
# environment in the ./env directory and install the necessary packages for
# FLFM.
env/bin/python:
	@echo "Creating conda environment..."
	conda create -y --prefix ./env python=3.11
	conda run -p ./env python -m pip install git+https://github.com/ssec-jhu/flfm.git

models/model1.pt: env/bin/python
	@echo "Exporting models..."
	mkdir -p models
	@for i in $(shell seq 1 15); do \
		echo "Exporting model$$i"; \
		conda run -p ./env python -m flfm.cli export --out models/model$$i.pt --n_steps $$i; \
	done

flfm-ij/src/main/resources/models/model1.pt: models/model1.pt
	@echo "Copying model1.pt to flfm-ij/src/main/resources/models"
	mkdir -p flfm-ij/src/main/resources/models
	cp models/model*.pt flfm-ij/src/main/resources/models/

flfm-ij/target/flfm_plugin.linux.jar: flfm-ij/src/main/resources/models/model1.pt
	mvn package -f flfm-ij/pom.xml -P linux-cuda

flfm-ij/target/flfm_plugin.windows.jar: flfm-ij/src/main/resources/models/model1.pt
	mvn package -f flfm-ij/pom.xml -P windows-cuda

linux: flfm-ij/target/flfm_plugin.linux.jar

windows: flfm-ij/target/flfm_plugin.windows.jar

clean:
	mvn clean -f flfm-ij/pom.xml
