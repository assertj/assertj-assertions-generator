#!/bin/sh
echo ''
echo 'Build AssertJ assertion generator ...'
echo ''
mvn clean install

echo ''
echo ''
echo 'Build AssertJ assertion generator maven plugin ...'
echo ''
cd ../assertj-assertions-generator-maven-plugin
mvn clean install

# go back to previous directory
cd -
