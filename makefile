.PHONY: clean build_linux build_windows



flfm/model1.pt:

	mkdir -p flfm-ij/src/main/resources/models
	cp flfm/model1.pt flfm-ij/src/main/resources/models/

flfm-ij/target/flfm_plugin.linux.jar:
	mvn package -f flfm-ij/pom.xml -P linux-cuda

flfm-ij/target/flfm_plugin.windows.jar:
	mvn package -f flfm-ij/pom.xml -P windows-cuda

build_linux: flfm-ij/target/flfm_plugin.linux.jar

build_windows: flfm-ij/target/flfm_plugin.windows.jar

clean:
	mvn clean -f flfm-ij/pom.xml