# Data Simulators

The DataSimulators project contains an application designed to simulate data coming from IOT devices.

# Build

git clone https://github.com/vakshorton/DataSimulators.git

cd DataSimulators/DeviceSimulator

mvn clean package

# Running Simulations

The Simulator should be located in the DeviceSimulator directory

USAGE:

java -jar simulator.jar arg1=Simulator-Type{BioReactor|FiltrationSystem} arg2=EntityId{1000} arg3={Simulation|Training}

EXAMPLES:

Cable Set Top Box Health: java -jar DeviceSimulator-0.0.1-SNAPSHOT-jar-with-dependencies.jar STB 1000 Simulation

Technician/Driver: java -jar DeviceSimulator-0.0.1-SNAPSHOT-jar-with-dependencies.jar Technician 1000 Simulation

BioReactor: java -jar DeviceSimulator-0.0.1-SNAPSHOT-jar-with-dependencies.jar BioReactor 1000 Simulation

Filtration System: java -jar DeviceSimulator-0.0.1-SNAPSHOT-jar-with-dependencies.jar FiltrationSystem 1000 Simulation

