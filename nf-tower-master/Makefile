config ?= compile

clean:
	./gradlew clean
	rm -rf .db
	docker rm nf-tower_db_1 || true

test:
ifndef class
	MICRONAUT_ENVIRONMENTS=mysql ./gradlew test
else
	MICRONAUT_ENVIRONMENTS=mysql ./gradlew test --tests ${class}
endif

build:
	./gradlew assemble
	./gradlew tower-backend:jibDockerBuild
	docker build -t tower-web:latest tower-web/

run:
	docker-compose up

deps:
	./gradlew -q tower-backend:dependencies --configuration ${config}

