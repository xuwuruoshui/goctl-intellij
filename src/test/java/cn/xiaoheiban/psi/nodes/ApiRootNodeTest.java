package cn.xiaoheiban.psi.nodes;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * 验证 API import 路径解析行为，避免跨目录公共类型再次出现无法跳转或错误标红。
 */
public class ApiRootNodeTest extends BasePlatformTestCase {

    /**
     * 验证从 sys 子目录通过 ../common/common.api 引用公共类型文件。
     */
    public void testFindFileByRelativePathSupportsParentDirectory() {
        // 1. 构造与真实项目一致的 api/sys 与 api/common 目录结构。
        PsiFile sourceFile = myFixture.addFileToProject("api/sys/user.api", "syntax = \"v1\"");
        PsiFile commonFile = myFixture.addFileToProject("api/common/common.api", "syntax = \"v1\"");

        // 2. 从 user.api 所在目录解析包含父目录段的 import 路径。
        VirtualFile sourceDirectory = sourceFile.getVirtualFile().getParent();
        VirtualFile resolvedFile = ApiRootNode.findFileByRelativePath(
                sourceDirectory,
                "../common/common.api"
        );

        // 3. 确认解析结果准确指向公共类型文件，保证 IDReq 等类型可被 PSI 索引找到。
        assertEquals(commonFile.getVirtualFile(), resolvedFile);
    }

    /**
     * 验证 Windows 风格反斜杠路径同样可以解析，避免不同平台表现不一致。
     */
    public void testFindFileByRelativePathSupportsWindowsSeparators() {
        // 1. 构造跨目录导入所需的源文件与目标文件。
        PsiFile sourceFile = myFixture.addFileToProject("api/auth/auth.api", "syntax = \"v1\"");
        PsiFile userFile = myFixture.addFileToProject("api/sys/user.api", "syntax = \"v1\"");

        // 2. 使用 Windows 风格路径执行解析。
        VirtualFile sourceDirectory = sourceFile.getVirtualFile().getParent();
        VirtualFile resolvedFile = ApiRootNode.findFileByRelativePath(
                sourceDirectory,
                "..\\sys\\user.api"
        );

        // 3. 确认路径规范化后仍能定位到目标 API 文件。
        assertEquals(userFile.getVirtualFile(), resolvedFile);
    }
}
