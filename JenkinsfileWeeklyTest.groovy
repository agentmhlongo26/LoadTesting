@Library('jenkins-utilities@master') _
def not = new dstvo.jenkinsutilities.Notify()
node('docker && isn') {
    not.notify('CVTestEngineerTeam@multichoice.com') {
        stage('Retrieving latest source') {
            checkout scm
        }
        stage('Running tests...') {
            new dstvo.jenkinsutilities.Docker().runMavenTests("mvn gatling:test -Dgatling.simulationClass=templates.SimpleTemplateSSL")
        }
    }
}