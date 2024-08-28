import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCompose
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.projectFeatures.dockerRegistry
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

    vcsRoot(GitTest)

    buildType(Build)

    features {
        dockerRegistry {
            id = "PROJECT_EXT_2"
            name = "Docker Registry"
            userName = "1"
            password = "credentialsJSON:19b1d2c4-63a5-41f4-a648-675e9030edb0"
        }
    }
}

object Build : BuildType({
    name = "build"

    params {
        param("branch", "main")
        param("git", "https://github.com/bnbn131/teamcity2.git")
    }

    vcs {
        root(GitTest)
    }

    steps {
        dockerCompose {
            name = "docker run"
            id = "docker_run"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            file = """src\docker-compose.yml"""
        }
        gradle {
            name = "cucumber run"
            id = "cucumber_run"
            tasks = "cucumber"
        }
        script {
            id = "simpleRunner"
            scriptContent = "docker ps"
        }
    }
})

object GitTest : GitVcsRoot({
    name = "git_test"
    url = "%git%"
    branch = "%branch%"
})
