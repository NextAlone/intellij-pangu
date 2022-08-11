package com.lilittlecat.plugin.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.PsiFile;
import com.lilittlecat.plugin.pangu.Pangu;

import java.text.MessageFormat;

import static com.lilittlecat.plugin.common.Constant.DISPLAY_NAME;

/**
 * @author LiLittleCat
 * @since 2022/8/6
 */
public class PanguFormatAction extends AnAction {

    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(DISPLAY_NAME, NotificationDisplayType.BALLOON, true);

    private static final Pangu PANGU = new Pangu();

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        PsiFile file = CommonDataKeys.PSI_FILE.getData(dataContext);
        if (editor == null || file == null) {
            return;
        }
        Document document = editor.getDocument();
        if (document.isWritable()) {
            SelectionModel selectionModel = editor.getSelectionModel();
            String selectedText = selectionModel.getSelectedText();
            if (selectedText == null) {
                // no selection, pangu all content of current file
                String text = document.getText();
                CommandProcessor.getInstance().executeCommand(file.getProject(), () ->
                        WriteAction.run(()-> document.setText(PANGU.formatText(text))), DISPLAY_NAME, null);
            } else if (!selectedText.isBlank()) {
                // selection, pangu selected text
                int selectionStart = selectionModel.getSelectionStart();
                int selectionEnd = selectionModel.getSelectionEnd();
                CommandProcessor.getInstance().executeCommand(file.getProject(), () ->
                        WriteAction.run(() ->
                                document.replaceString(selectionStart, selectionEnd, PANGU.formatText(selectedText))), DISPLAY_NAME, null);
            }
            notification(MessageFormat.format("{0}: \"{1}\"success.", DISPLAY_NAME, file.getName()), MessageType.INFO, e.getProject());
        } else {
            // notification: cannot edit current file
            notification(MessageFormat.format("{0} fail: \"{1}\"is not writable.", DISPLAY_NAME, file.getName()), MessageType.WARNING, e.getProject());
        }
    }

    /**
     * notification
     *
     * @param message     message to show
     * @param messageType message type
     * @param project     current project
     */
    private static void notification(String message, MessageType messageType, Project project) {
        Notification notification = NOTIFICATION_GROUP.createNotification(message, messageType);
        Notifications.Bus.notify(notification, project);
    }

}