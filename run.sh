#!/bin/bash

# Build the project if needed
./gradlew build

# Run the JAR
java -jar build/libs/jarDevRunner.jar