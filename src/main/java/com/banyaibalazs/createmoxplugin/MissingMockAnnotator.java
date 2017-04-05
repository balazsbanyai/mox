package com.banyaibalazs.createmoxplugin;

import com.intellij.codeInsight.completion.AllClassesGetter;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiConstructorCall;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

public class MissingMockAnnotator implements Annotator {


    class ProcessorImpl implements Processor<PsiClass> {

        private final PsiElement element;
        private final AnnotationHolder holder;

        public ProcessorImpl(PsiElement element, AnnotationHolder holder) {
            this.element = element;
            this.holder = holder;
        }

        public boolean process(PsiClass psiClass) {
            if (psiClass.getQualifiedName().startsWith("org.mockito.Mock")) {
                doActualAnnotation();
                return false;
            }
            return true;
        }

        private void doActualAnnotation() {
            PsiConstructorCall psiConstructorCall = ((PsiConstructorCall) element);
            int countOfActualArgs = psiConstructorCall.getArgumentList().getExpressions().length;
            PsiParameterList parameterList = psiConstructorCall.resolveConstructor().getParameterList();
            if (countOfActualArgs != parameterList.getParametersCount()) {
                PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class);
                holder
                        .createErrorAnnotation(element.getTextRange(), "Create mocks for this element")
                        .registerFix(new CreateMocksQuickFix(clazz, psiConstructorCall));
            }
        }
    }
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {

        if (element instanceof PsiConstructorCall) {
            Project project = element.getProject();
            Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(element.getContainingFile().getVirtualFile());

            Processor p = new ProcessorImpl(element, holder);
            AllClassesGetter.processJavaClasses(
                    PrefixMatcher.ALWAYS_TRUE,
                    project,
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module),
                    p
            );
        }
    }
}
