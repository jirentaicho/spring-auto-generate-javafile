package com.volkruss

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import org.apache.commons.lang3.StringUtils
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

import java.nio.file.Path
import java.nio.file.Paths

class ArtisanTask extends DefaultTask {
    @TaskAction
    def doTask(){

        // 必須引数チェックを行う
        // サブシステム名
        if (!project.hasProperty("subSystem")) {
            println("subSystem must not be null")
            return
        }
        // 必須項目チェック
        // ファイル名
        if (!project.hasProperty("func")) {
            println("func must not be null")
            return
        }

        def ext = project.extensions.findByType(ArtisanExtension)

        // テンプレートファイル
        def String templateFile = ext.templateFile

        // 出力するフォルダのパスの設定
        // この辺はエクステンションファイルから読み込むのが良い
        def String domainProjectName = ext.domainProjectName
        def String srcDirName = ext.srcDirName
        def String domainBaseName = ext.domainBaseName

        // 出力ファイルのパッケージ/サブシステム/ファイル名の設定
        def String pacakege = ext.pacakege
        def String subsystem = project.subSystem.toString()
        def String funcName = StringUtils.capitalize(project.func.toString())
        // doublecheck → Doublecheck　に変換されます

        // 出力ファイルの拡張子
        def String suffix = ".java"

        // 変数の設定
        Map<String,Object> map = new HashMap<>();
        map.put("domainBaseName",domainBaseName)
        map.put("dtoPackageName",pacakege)
        map.put("subSystem",subsystem)
        map.put("upperFuncName",funcName)

        //　出力先のフォルダパス
        def domainTarget = "${domainProjectName}/${srcDirName}/${domainBaseName}"
        generate(templateFile, getTargetPath(domainTarget, "/${pacakege}/${subsystem}/${funcName}", suffix), map)

        println("--------end-------")
    }

    Path getTargetPath(String _target, String _fileName, String suffix) {
        def target = StringUtils.replace(_target, ".", "/")
        def sb = new StringBuilder().append(target)
        def fileName = StringUtils.replace(_fileName, ".", "/")
        return Paths.get(sb.toString(), "${fileName}${suffix}")
    }

    //　Thymeleafにて取得したStringの内容を、設定したパスにUTF-8で書き込む
    void generate(String template, Path path, Map<String, Object> objects) {
        def body = processTemplate(template, objects)
        def f = path.toFile()
        f.parentFile.mkdirs()
        f.createNewFile()
        f.write(body, "UTF-8")
    }

    String processTemplate(String template, Map<String, Object> objects) {
        def resolver = new ClassLoaderTemplateResolver()
        resolver.setTemplateMode("TEXT")
        resolver.setCharacterEncoding("UTF-8")

        def templateEngine = new TemplateEngine()
        templateEngine.setTemplateResolver(resolver)

        def context = new Context()
        // 引数で受取った値を元に設定したMapから変数を設定する
        if (objects != null && !objects.isEmpty()) {
            objects.each { key, value ->
                context.setVariable(key, value)
            }
        }
        // 指定したテンプレート（テキストデータ）に対してcontextの内容を埋め込んでStringを返す（Modelのような感じ）
        return templateEngine.process(template, context)
    }
}
