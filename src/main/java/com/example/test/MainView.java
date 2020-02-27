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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.router.Route;


@Route("")
public class MainView extends VerticalLayout {

	Grid<Person> childrenGrid;
	VerticalLayout form;
	Dialog dialog;

	public MainView() {

		final ParentDataProvider dataProvider = new ParentDataProvider();
		setupChildrenGrid(dataProvider);

		final Crud<Parent> crud = new Crud<>(Parent.class, createParentEditor());		
		
		crud.setDataProvider(dataProvider);
		crud.addSaveListener(e -> {
			dataProvider.persist(e.getItem());
			dialog.close();
			if(dialog.getParent().isPresent())
				dialog.getParent().ifPresent(p -> System.out.println(p.toString()));
			else 
				System.out.println("Dialog has no parent");
		});
		crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

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
		final Div error = new Div();
		dialog = new Dialog();
		dialog.add(new Label("At least one child needed"));
		error.getElement().getStyle().set("color", "red");
		form = new VerticalLayout(nameField, error, dialog, this.childrenGrid);

		final Binder<Parent> binder = new Binder<>(Parent.class);
		binder.bind(nameField, Parent::getName, Parent::setName);

		final MultiSelect<Grid<Person>, Person> multiselect = this.childrenGrid.asMultiSelect();
		//binder.forField(multiselect).withValidator(children -> !children.isEmpty(), "At least one child needed").withStatusLabel(error).bind(Parent::getChildren, Parent::setChildren);
		binder.forField(multiselect).withValidator(children -> !children.isEmpty(), "At least one child needed")
				.withValidationStatusHandler(status -> {
					if (status.isError()) {

						dialog.open();
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
