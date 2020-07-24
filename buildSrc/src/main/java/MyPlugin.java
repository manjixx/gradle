import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * MyPlugin 插件
 *
 * @author: manji
 * @data: 2020/7/24
 */
public class MyPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        for (int i = 0; i < 10; i++) {
            project.task("task-" + i);
        }
    }
}
