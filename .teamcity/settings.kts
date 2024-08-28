import agents
import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.AbsoluteId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.BuildFailureOnText
import jetbrains.buildServer.configs.kotlin.v2019_2.failureConditions.failOnText
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
import readScript

class Docker (
    private val jobName: String,
    private val jobDesc: String,
    private val vcsRepository: GitVcsRoot,
    private val vcsBranchFilter: String,
    private val parentFolderName: String,
    private val depParJobId: String,
    private val depArtifactJobId: String
) : BuildType({
    name = jobName
    id("${jobName}_${parentFolderName}".toExtId())
    description = jobDesc

    vcs {
        root(vcsRepository)
        cleanCheckout = true
        branchFilter = vcsBranchFilter
    }

    params {
        param("app_type", "%dep.${depParJobId}.app_type%")
        param("service_version", "%dep.${depParJobId}.service_version%")
        param("build_artifacts", "docker")
        param("DOCKER_BASE_IMAGE", "java/liberica-openjdk-alpine-musl:17.0.6")
    }
    steps {
        script {
            name = "Get cache"
            scriptContent = readScript("files/scripts/build/backend/get_cache.sh")
        }
        script {
            name = "Delete artifact from Nexus-CD"
            scriptContent = readScript("files/scripts/build/delete_docker_artifact_from_nexus_cd.sh")
        }
        script {
            name = "Docker login"
            scriptContent = readScript("files/scripts/docker_login.sh")
        }
        script {
            name = "Docker configs"
            scriptContent = readScript("files/scripts/build/backend/docker_configs.sh")
        }
        script {
            name = "Docker build"
            scriptContent = readScript("files/scripts/build/backend/docker_build.sh")
        }
        script {
            name = "Docker push"
            scriptContent = readScript("files/scripts/build/docker_push.sh")
        }
        script {
            name = "Associate tag with version with artifacts in Nexus-CD"
            executionMode = BuildStep.ExecutionMode.RUN_ON_FAILURE
            scriptContent = readScript("files/scripts/build/associate_tag_with_version_with_artifact_in_nexus_cd.sh")

            conditions {
                equals("qg", "true")
            }
        }
        script{
            name = "Associate tag TO_DMZ with artifacts in Nexus-CD"
            scriptContent = readScript("files/scripts/build/associate_tag_to_dmz_with_artifact_in_nexus_cd.sh")

            conditions {
                equals("deploy_to_dmz", "true")
                equals("qg", "true")
            }
        }
        script {
            name = "Docker logout"
            executionMode = BuildStep.ExecutionMode.ALWAYS
            scriptContent = readScript("files/scripts/docker_logout.sh")
        }
    }

    dependencies {
        artifacts(AbsoluteId(depArtifactJobId)) {
            artifactRules = "**/*"
        }
    }

    features {
        perfmon {
        }
    }

    failureConditions {
        executionTimeoutMin = 15
        failOnText {
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "Out of system resources"
            failureMessage = "Out of system resources"
            reverse = false
            stopBuildOnFailure = true
        }
    }

    requirements { agents("build") }
})
