import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubConnection
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

project {
    description = "Contains all other projects"

    vcsRoot(Test)

    features {
        buildReportTab {
            id = "PROJECT_EXT_1"
            title = "Code Coverage"
            startPage = "coverage.zip!index.html"
        }
        githubConnection {
            id = "PROJECT_EXT_3"
            displayName = "GitHub.com"
            clientId = "bnbn131"
            clientSecret = "credentialsJSON:91cda76b-eca9-4c88-ad94-d0f5466407b2"
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }

    subProject(Teamcity2)
}

object Test : GitVcsRoot({
    name = "test"
    url = "https://github.com/bnbn131/teamcity2.git"
    branch = "main"
    authMethod = password {
        userName = "bnbn131"
        password = "credentialsJSON:3cbbbeeb-27d3-43ee-9aa7-34bf14bdbb22"
    }
})


object Teamcity2 : Project({
    name = "Teamcity2"

    vcsRoot(Teamcity2_HttpsGithubComBnbn131teamcity2gitRefsHeadsMain1)
    vcsRoot(Teamcity2_HttpsGithubComBnbn131teamcity2gitRefsHeadsMain)

    buildType(Teamcity2_Build)
})

object Teamcity2_Build : BuildType({
    name = "Build"

    vcs {
        root(Teamcity2_HttpsGithubComBnbn131teamcity2gitRefsHeadsMain1)
    }

    steps {
        maven {
            id = "Maven2"
            goals = "clean test"
            pomLocation = ".teamcity/pom.xml"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})

object Teamcity2_HttpsGithubComBnbn131teamcity2gitRefsHeadsMain : GitVcsRoot({
    name = "https://github.com/bnbn131/teamcity2.git#refs/heads/main"
    url = "https://github.com/bnbn131/teamcity2.git"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "bnbn131"
        password = "credentialsJSON:91cda76b-eca9-4c88-ad94-d0f5466407b2"
    }
})

object Teamcity2_HttpsGithubComBnbn131teamcity2gitRefsHeadsMain1 : GitVcsRoot({
    name = "https://github.com/bnbn131/teamcity2.git#refs/heads/main (1)"
    url = "https://github.com/bnbn131/teamcity2.git"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
})
