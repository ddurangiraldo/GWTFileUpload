package com.creditsuisse.fileUpload.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.widgetideas.client.ProgressBar;

import org.moxieapps.gwt.uploader.client.Uploader;
import org.moxieapps.gwt.uploader.client.events.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Uploader Image Button with multiple file selection, drag and drop support,
 * and GWT Progress Bars example of GWT SWF Uploader.
 */
public class FileUploadDragAndDrop implements EntryPoint {

	public void onModuleLoad() {
		final VerticalPanel progressBarPanel = new VerticalPanel();
		final Map<String, ProgressBar> progressBars = new LinkedHashMap<String, ProgressBar>();
		final Map<String, Image> cancelButtons = new LinkedHashMap<String, Image>();
		final Uploader uploader = new Uploader();
		uploader.setUploadURL(GWT.getModuleBaseURL()+"fileupload")
				.setButtonImageURL(GWT.getModuleBaseURL() + "resources/images/buttons/upload_new_version_button.png")
				.setButtonWidth(133).setButtonHeight(22).setFileSizeLimit("50 MB").setButtonCursor(Uploader.Cursor.HAND)
				.setButtonAction(Uploader.ButtonAction.SELECT_FILES).setFileQueuedHandler(new FileQueuedHandler() {
					public boolean onFileQueued(final FileQueuedEvent fileQueuedEvent) {
						// Create a Progress Bar for this file
						final ProgressBar progressBar = new ProgressBar(0.0, 1.0, 0.0,
								new CancelProgressBarTextFormatter());
						progressBar.setTitle(fileQueuedEvent.getFile().getName());
						progressBar.setHeight("18px");
						progressBar.setWidth("200px");
						progressBars.put(fileQueuedEvent.getFile().getId(), progressBar);

						// Add Cancel Button Image
						final Image cancelButton = new Image(
								GWT.getModuleBaseURL() + "resources/images/icons/cancel.png");
						cancelButton.setStyleName("cancelButton");
						cancelButton.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								uploader.cancelUpload(fileQueuedEvent.getFile().getId(), false);
								progressBars.get(fileQueuedEvent.getFile().getId()).setProgress(-1.0d);
								cancelButton.removeFromParent();
							}
						});
						cancelButtons.put(fileQueuedEvent.getFile().getId(), cancelButton);

						// Add the Bar and Button to the interface
						HorizontalPanel progressBarAndButtonPanel = new HorizontalPanel();
						progressBarAndButtonPanel.add(progressBar);
						progressBarAndButtonPanel.add(cancelButton);
						progressBarPanel.add(progressBarAndButtonPanel);

						return true;
					}
				}).setUploadProgressHandler(new UploadProgressHandler() {
					public boolean onUploadProgress(UploadProgressEvent uploadProgressEvent) {
						ProgressBar progressBar = progressBars.get(uploadProgressEvent.getFile().getId());
						progressBar.setProgress(
								(double) uploadProgressEvent.getBytesComplete() / uploadProgressEvent.getBytesTotal());
						return true;
					}
				}).setUploadCompleteHandler(new UploadCompleteHandler() {
					public boolean onUploadComplete(UploadCompleteEvent uploadCompleteEvent) {
						cancelButtons.get(uploadCompleteEvent.getFile().getId()).removeFromParent();
						uploader.startUpload();
						return true;
					}
				}).setFileDialogStartHandler(new FileDialogStartHandler() {
					public boolean onFileDialogStartEvent(FileDialogStartEvent fileDialogStartEvent) {
						if (uploader.getStats().getUploadsInProgress() <= 0) {
							// Clear the uploads that have completed, if none
							// are in process
							progressBarPanel.clear();
							progressBars.clear();
							cancelButtons.clear();
						}
						return true;
					}
				}).setFileDialogCompleteHandler(new FileDialogCompleteHandler() {
					public boolean onFileDialogComplete(FileDialogCompleteEvent fileDialogCompleteEvent) {
						if (fileDialogCompleteEvent.getTotalFilesInQueue() > 0) {
							if (uploader.getStats().getUploadsInProgress() <= 0) {
								uploader.startUpload();
							}
						}
						return true;
					}
				}).setFileQueueErrorHandler(new FileQueueErrorHandler() {
					public boolean onFileQueueError(FileQueueErrorEvent fileQueueErrorEvent) {
						Window.alert("Upload of file " + fileQueueErrorEvent.getFile().getName() + " failed due to ["
								+ fileQueueErrorEvent.getErrorCode().toString() + "]: "
								+ fileQueueErrorEvent.getMessage());
						return true;
					}
				}).setUploadErrorHandler(new UploadErrorHandler() {
					public boolean onUploadError(UploadErrorEvent uploadErrorEvent) {
						cancelButtons.get(uploadErrorEvent.getFile().getId()).removeFromParent();
						Window.alert("Upload of file " + uploadErrorEvent.getFile().getName() + " failed due to ["
								+ uploadErrorEvent.getErrorCode().toString() + "]: " + uploadErrorEvent.getMessage());
						return true;
					}
				});

		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.add(uploader);

		if (Uploader.isAjaxUploadWithProgressEventsSupported()) {
			final Label dropFilesLabel = new Label("Drop Files Here");
			dropFilesLabel.setStyleName("dropFilesLabel");
			dropFilesLabel.addDragOverHandler(new DragOverHandler() {
				public void onDragOver(DragOverEvent event) {
					if (!uploader.getButtonDisabled()) {
						dropFilesLabel.addStyleName("dropFilesLabelHover");
					}
				}
			});
			dropFilesLabel.addDragLeaveHandler(new DragLeaveHandler() {
				public void onDragLeave(DragLeaveEvent event) {
					dropFilesLabel.removeStyleName("dropFilesLabelHover");
				}
			});
			dropFilesLabel.addDropHandler(new DropHandler() {
				public void onDrop(DropEvent event) {
					dropFilesLabel.removeStyleName("dropFilesLabelHover");

					if (uploader.getStats().getUploadsInProgress() <= 0) {
						progressBarPanel.clear();
						progressBars.clear();
						cancelButtons.clear();
					}

					uploader.addFilesToQueue(Uploader.getDroppedFiles(event.getNativeEvent()));
					event.preventDefault();
				}
			});
			verticalPanel.add(dropFilesLabel);
		}

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.add(verticalPanel);
		horizontalPanel.add(progressBarPanel);
		horizontalPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		horizontalPanel.setCellHorizontalAlignment(uploader, HorizontalPanel.ALIGN_LEFT);
		horizontalPanel.setCellHorizontalAlignment(progressBarPanel, HorizontalPanel.ALIGN_RIGHT);

		// noinspection GwtToHtmlReferences
		RootPanel.get().add(horizontalPanel);
	}

	protected class CancelProgressBarTextFormatter extends ProgressBar.TextFormatter {
		@Override
		protected String getText(ProgressBar bar, double curProgress) {
			if (curProgress < 0) {
				return "Cancelled";
			}
			return ((int) (100 * bar.getPercent())) + "%";
		}
	}

}