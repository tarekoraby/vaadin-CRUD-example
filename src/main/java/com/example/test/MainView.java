package com.example.test;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudEditorPosition;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

	Grid<Person> childrenGrid;
	boolean errorProne;

	public MainView() {

		final ParentDataProvider dataProvider = new ParentDataProvider();
		setupChildrenGrid(dataProvider);

		final Crud<Parent> crud = new Crud<>(Parent.class, createParentEditor());

		crud.setDataProvider(dataProvider);

		// Setting errorProne to true ensures that only when the editor is being
		// opened could the validation fail
		crud.addEditListener(e -> errorProne = true);

		// Otherwise (i.e. if the editor is being closed due to save, delete, or
		// cancel) the validation will always pass due to errorProne = false
		crud.addSaveListener(e -> {
			dataProvider.persist(e.getItem());
			errorProne = false;
		});
		crud.addDeleteListener(e -> {
			dataProvider.delete(e.getItem());
			errorProne = false;
		});
		crud.addCancelListener(e -> errorProne = false);	

		crud.setEditorPosition(CrudEditorPosition.ASIDE);
		crud.setEditOnClick(true);
		crud.setOpened(true);
		crud.setSizeFull();
		crud.getGrid().setColumns("name");
		setSizeFull();
		add(crud);
	}

	private CrudEditor<Parent> createParentEditor() {
		final TextField nameField = new TextField("Parent name");

		String errorMsg = "At least one child needed";
		
		final Div errorDiv = new Div();
		errorDiv.setText(errorMsg);
		errorDiv.getElement().getStyle().set("color", "red");
		errorDiv.setVisible(false);

		Dialog errorDialog = new Dialog();
		errorDialog.add(new Label(errorMsg));

		VerticalLayout form = new VerticalLayout(nameField, errorDiv, this.childrenGrid);

		final Binder<Parent> binder = new Binder<>(Parent.class);
		binder.bind(nameField, Parent::getName, Parent::setName);

		final MultiSelect<Grid<Person>, Person> multiselect = this.childrenGrid.asMultiSelect();
		binder.forField(multiselect)
				.withValidator(children -> !errorProne || !children.isEmpty(), "At least one child needed")
				.withValidationStatusHandler(status -> {
					if (status.isError()) {
						errorDiv.setVisible(true);
						errorDialog.open();
					} else {
						errorDiv.setVisible(false);
						errorDialog.close();
					}
				}).bind(Parent::getChildren, Parent::setChildren);
		return new BinderCrudEditor<>(binder, form);
	}

	private void setupChildrenGrid(final ParentDataProvider dataProvider) {
		this.childrenGrid = new Grid<>(Person.class);
		this.childrenGrid.setSelectionMode(SelectionMode.MULTI);
		final List<Person> allChildren = new ArrayList<>();
		dataProvider.DATABASE.stream().forEach(parent -> allChildren.addAll(parent.getChildren()));
		this.childrenGrid.setItems(allChildren);
	}
}
