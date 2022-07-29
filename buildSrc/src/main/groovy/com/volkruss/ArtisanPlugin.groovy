package com.volkruss

import org.gradle.api.Plugin
import org.gradle.api.Project

class ArtisanPlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {
        project.extensions.create("artisan",ArtisanExtension)
        project.task("artisan", type: ArtisanTask)
    }
}
