# gradle
gradle learn


*https://docs.gradle.org/current/userguide/plugins.html*

## Using Gradle Plugins
gradle 核心部分有意为正真的构建提供很少.有用的特性都通过插件去实现.
插件可以添加  新task/domain对象/约定/扩展核心对象/其他插件的对象
###### 下面讨论插件相关术语和概念和如何使用插件.

### What-plugins-do
插件应用于项目,就相当于允许插件的能力被项目继承.

* 扩展原型 (添加可以配置的新DSL元素)
* 根据预定配置项目 (添加新任务或配置合理的默认值)
* 应用特定配置 (添加组织仓库或执行标准)

应用插件的好处
* 复用,减少维护相似逻辑在不同项目上的开销 
* 模块化,便于理解和条理清晰 
* 封装命令行逻辑,允许构建脚本尽可能的具有声明性

### Types of plugins
* #### Binary plugin (二进制插件)
  > 可以通过实现插件接口进行编程  
  > 也可以通过Gradle的一种DSL语言声明编写

    二进制插件可以在构建脚本中,也可以在项目结构层次或者外部插件jar包中
                               
* #### Script plugin (脚本插件)
  > 其他通常使用声明式方法进一步配置构建的构建脚本

    通常在构建中使用,通常是外部化,可以通过远程访问  

通常情况下,插件可能是script plugin(方便编写),随着插件代码的壮大,以及功能的全面性,就会被改成binary plugin发布出来,易于测试,在不同的组织和项目之间共享

### Using plugins

Gradle使用插件中封装的逻辑通过 two steps:
* 解析插件
  > 解析插件 == 找到包含给定插件的正确的版本,添加到script的classpath.  
  > 插件解析完成,就能在build script中使用其API.  
  > 插件是如何解析的 == 当你应用插件的时候,其实是插件本身通过你提供的特殊的路径或者URL自己解析  
  > 核心binary plugin 作为Gradle distribution(发布)的一部分会自动解析
* 将插件应用于目标(通常是Project)
  > 应用插件的本质就是执行插件的 apply方法中的东西来增强你的项目  
  > 应用插件都是幂等的,所以不用担心多次使用造成的影响

使用插件常见的就是解析插件然后将插件应用于项目,因此推荐构建人使用 plugins DSL 在一个步骤里完成resolve and apply

### Binary plugin

>通过插件id(插件全局唯一标识/名称)应用插件,Gradle的核心插件特殊在他们提供了简短的名称.
所有其他二进制插件必须使用插件ID的全限定形式,不过还有一些旧的插件使用的是简短非限定形式.
插件id放哪取决于你用的是插件DSL方式还是 buildScript block模式引入  

  #### Locations of binary plugins - 插件位置
   简单说,插件就是实现了Plugin结构的类,核心插件作为gradle的一部分自动解析,那么非核心的插件使用前需要解析,有下面几种方式
* 使用plugin DSL引用插件门户网站或者客户端自己的插件仓库
* 从被定义为buildScript依赖项的外部jar包引入插件
* 在项目的buildSrc目录下定义插件的源文件,引入插件
* 在build script里声明一个'内部类'定义插件用来引入
* 更多方法见 Developing Custom Gradle Plugins
  #### Applying plugins with the plugins DSL (DSL定义插件)
    DSL提供了简洁方便的方式去声明一个插件,通过Gradle plugin portal提供的核心插件和社区插件的访问方式.下面是插件块配置实例
    ```groovy    
    plugins {
        id 'java'  // 核心插件可以简写 全称 org.gradle.java
        id 'com.jfrog.bintray' version '0.4.1'   //社区插件必须指定版本
        id "org.company.myplugin" version "1.3" apply false // 禁用自动应用到当前脚本
    }
    ```
  ##### Limitations of the plugins DSL (DSL局限性)
    并不是最方便的方法,DSL能够很早确定需要使用的插件. 让Gradle做更智能的事情 
    * 优化加载和重用
    * 允许不同插件使用不同版本的依赖项
    * 提供编辑帮助给编辑者关于构建脚本中的潜在属性和值的详细信息  
    
    要求在执行其余脚本之前提供一种让Gradle能快速容易的提取插件的方式.另外还要求插件的定义方式是静态的.
    在plugin{}块和apply()两种机制之间存在关键的不同, 存在一些约束,一部分是暂时的仍在开发中的,一部分是新方法具有的.
    ##### Constrained Syntax (约束语法)
    不支持任意代码,为了保证幂等性和无副作用.
    而且plugin{}只能是buildScript的顶级语句,不能嵌套在另一个结构内
    ##### Can only be used in build scripts and settings file (只能在构建脚本和设置文件中使用)
    plugin{}块目前只能在项目的构建脚本和settings.gradle文件中使用。不能在脚本插件或初始化脚本中使用。
    Gradle的未来版本将删除此限制。
    如果plugins{}块的限制令人望而却步，建议的方法是使用buildscript{}块来应用插件。
    
  #### Applying plugins to subprojects (在子项目中应用插件)
    如果你创建一个多项目构建,可能想将插件应用于部分或者全部子项目,但不作用于根或者master项目.
    插件块的默认对插件是立马解析和应用.但是你可以使用 apply false语法来告诉Gradle不要应用插件到当前项目.
    然后可以在 subprojects block中使用 apply plugin:<<plugin id>>或者在子项目中的build script中使用plugin{} block块
  ```groovy 
  //>groovy settings.gradle
    include 'helloA'
    include 'helloB'
    include 'goodbyeC'
  //>groovy build.gradle
    plugins {
        id 'com.example.hello' version '1.0.0' apply false
        id 'com.example.goodbye' version '1.0.0' apply false
    }
    subprojects {
          if (name.startsWith('hello')) {
              apply plugin: 'com.example.hello'
          }
    }
  //>groovy goodbyeC/build.gradle
    plugins {
        id 'com.example.goodbye'
    }
  ```

  #### Applying plugins from the buildSrc directory (从buildSrc目录应用插件)
   你可以应用存在于buildSrc目录中具有Id的插件.
   下面的示例如何绑定buildSrc中的插件实现类my.MyPlugin,id定义为"my-plugin"
  ```groovy
  //> buildSrc/build.gradle
    plugins {
        id 'java-gradle-plugin'
    }
    gradlePlugin {
        plugins {
            myPlugins {
                id = 'my-plugin'
                implementationClass = 'my.MyPlugin'
            }
        }
    }
  //> build.gradle
    plugins {
        id 'my-plugin'
    }
  ``` 
 
  #### Plugin Management (插件管理)
   pluginManagement {} block只能出现在setting.gradle文件中且必须是文件的第一块.或者在一个init脚本中
   按项目和全局配置pluginManagement
  ```groovy
  //> settings.gradle
    pluginManagement {
        plugins {
        }
        resolutionStrategy {
        }
        repositories {
        }
    }
  //> init.gradle
    settingsEvaluated { settings ->
        settings.pluginManagement {
            plugins {
            }
            resolutionStrategy {
            }
            repositories {
            }
        }
    }
  ```   
  #### Custom Plugin Repositories (自定义插件存储库)
   默认情况下DSL从Gradle Plugin Portal解析插件.
   但是如果你还想从私有Maven或者ivy库中解析插件,或是为了特定的插件功能,亦或是为了能更好的管理构建中使用到的插件.
   指定自定义插件存储库,需要在pluginManagement{}块里通过repositories{}指定
   ```groovy
   //> settings.gradle
    pluginManagement {
        repositories {
            maven {
                url '../maven-repo'
            }
            gradlePluginPortal()
            ivy {
                url '../ivy-repo'
            }
        }
    }
   ```  
  这个告诉Gradle先去指定的maven仓库查找需要的插件,如果没有才去插件门户找需要的插件.
  如果你不想去插件门户找的话,可以省略gradlePluginPortal() 直接去ivy库找.
  #### Plugin Version Management (插件版本管理)
   pluginManagement{}中的plugins{}块允许将构建的所有插件版本定义在一个位置.
   然后可以通过plugins{}块将id的插件应用于任何构建脚本.
   通过这种方式设置插件版本的好处之一是,pluginManagement.plugins{}的语法与构建脚本plugins{}块的约束语法不同.
   这允许从gradle.properties中获取插件版本,或通过其他机制加载.
  ```groovy
  //>settings.gradle
   pluginManagement {
     plugins {
           id 'com.example.hello' version "${helloPluginVersion}"
       }
   }
  //>gradle.properties
    helloPluginVersion='1.0.0'
  ```
  插件版本从gradle.properties加载并在设置脚本中进行配置,允许将插件添加到任何项目而无需指定版本.
  
  #### Plugin Resolution Rules (插件解析规则)
   插件解析规则可让您修改在plugin{}块中发出的插件请求,例如更改请求的版本或明确指定实现工件坐标.
   要添加解析规则,在pluginManagement{}块内使用resolutionStrategy{}.
  ```groovy
  //>settings.gradle 
   pluginManagement {
       resolutionStrategy {
           eachPlugin {
               if (requested.id.namespace == 'com.example') {
                   useModule('com.example:sample-plugins:1.0.0')
               }
           }
       }
       repositories {
           maven {
               url '../maven-repo'
           }
           gradlePluginPortal()
           ivy {
               url '../ivy-repo'
           }
       }
   }
  ```   
  这告诉Gradle使用指定的插件实现构件,而不是使用其从插件ID到Maven / Ivy坐标的内置默认映射.
  自定义Maven和Ivy插件存储库除了实际实现插件的工件外,还必须包含插件标记工件.
  有关将插件发布到自定义存储库的更多信息,请阅读Gradle Plugin Development Plugin [https://docs.gradle.org/current/userguide/java_gradle_plugin.html#java_gradle_plugin]
  有关使用pluginManagement{}block的完整文档，请参见PluginManagementSpec [https://docs.gradle.org/current/javadoc/org/gradle/plugin/management/PluginManagementSpec.html]
  
  #### Plugin Marker Artifacts (插件仓库发布)
   由于plugin{}-DSL-block仅允许通过全局唯一ID和版本号声明插件,因此Gradle需要一种方法来查找插件实现工件的坐标.
   为此Gradle将查找带坐标plugin.id : plugin.id.gradle.plugin : plugin.version的插件标记工件.该标记需要依赖于实际的插件实现.这些标记的发布由java-gradle-plugin自动执行.
   例如,以下来自sample-plugins项目的完整示例显示了如何使用java-gradle-plugin、maven-publish插件和ivy-publish插件的组合将com.example.hello插件和com.example.goodbye插件发布到Ivy和Maven存储库.
  ```groovy
  //>build.gradle
   plugins {
       id 'java-gradle-plugin'
       id 'maven-publish'
       id 'ivy-publish'
   }
   group 'com.example'
   version '1.0.0'
   gradlePlugin {
       plugins {
           hello {
               id = 'com.example.hello'
               implementationClass = 'com.example.hello.HelloPlugin'
           }
           goodbye {
               id = 'com.example.goodbye'
               implementationClass = 'com.example.goodbye.GoodbyePlugin'
           }
       }
   }
   publishing {
       repositories {
           maven {
               url '../../consuming/maven-repo'
           }
           ivy {
               url '../../consuming/ivy-repo'
           }
       }
   }
  ```
  运行流程示意图 [https://docs.gradle.org/current/userguide/img/pluginMarkers.png]
  
  #### Legacy Plugin Application (旧版插件应用)
   随着DSL的引入,用户几乎没有理由使用应用插件的旧方法.以防万一构建者由于工作方式受到限制不能使用DSL,在此记录.
  
  #### Applying Binary Plugins (应用二进制插件)
   可以使用简单id来应用插件,下面情况就是使用简称'java'来应用javaPlugin
   除了使用id还可以通过简单的指定插件类来应用插件
  ```groovy
    apply plugin: 'java'
    apply plugin: JavaPlugin
  ```  
   上面的JavaPlugin指的就是 Class JavaPlugin,不需要导入class因为org.gradle.api.plugins包在所有构建中会自动导入
  #### Applying plugins with the buildscript block (使用带有buildscript block的插件)
   可以把二进制插件打成外部jar包,通过将jar包添加到build-script的classpath中来应用插件jar包.
   外部jar包可以在buildscript{} block中通过classpath的方式添加到 如下:
   ```groovy
   //> build.gradle
    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:0.4.1'
        }
    }
    apply plugin: 'com.jfrog.bintray'
   ```
  
  
### Script plugin
> script 插件会从本地或者远程文件中加载可被解析的脚本并应用,文件系统相对于项目目录,远程系统则通过HTTP URL指定. 可以将多个脚本应用于指定目标  

```groovy
 apply from: 'other.gradle'
```

### Finding community plugins - 插件社区


### More on plugins - 更多插件相关信息