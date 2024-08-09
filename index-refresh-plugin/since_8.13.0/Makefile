COMMA:= ,

GRADLEW = "../../gradlew"
GRADLE_PROJECT_PATH = ":index-refresh-plugin:since_8.13.0"
VERSIONS=$(shell $(GRADLEW) --quiet $(GRADLE_PROJECT_PATH):supportedVersions)
VERSIONS_LIST=$(subst $(COMMA), ,$(VERSIONS:v%=%))

build: $(foreach version,$(VERSIONS_LIST),build-$(version))

build-%: VERSION=$(*)
build-%:
	 $(GRADLEW) -PelasticsearchVersion=$(VERSION) $(GRADLE_PROJECT_PATH):releasePlugin