package org.eclipse.editor.huppaal;

import static org.eclipse.editor.EditorUtil.nvl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.editor.editor.Connector;
import org.eclipse.editor.editor.Diagram;
import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.editor.EditorFactory;
import org.eclipse.editor.editor.EndPoint;
import org.eclipse.editor.editor.State;
import org.eclipse.editor.huppaal.model.Component;
import org.eclipse.editor.huppaal.model.Entry;
import org.eclipse.editor.huppaal.model.Globalinit;
import org.eclipse.editor.huppaal.model.Hta;
import org.eclipse.editor.huppaal.model.Label;
import org.eclipse.editor.huppaal.model.Location;
import org.eclipse.editor.huppaal.model.Template;
import org.eclipse.editor.huppaal.model.Transition;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class XmlSerializerTest {
	XmlSerializer xmlSerializer;

	@Before
	public void before() {
		xmlSerializer = new XmlSerializer();
	}

	@Test
	public void toXml() throws Exception {
		StringWriter sw = new StringWriter();
		xmlSerializer.toXml(sw, createInitialState("A"));
		assertTrue(sw.toString().length() > 0);
	}

	@Test
	public void noInitialState() throws Exception {
		try {
			xmlSerializer.generateModel();
			fail();
		} catch (Exception e) {
			assertEquals("Model must have exactly one initial state.", e.getMessage());
		}
	}
	
	@Test
	public void onlyInitialState() throws Exception {
		State state = createInitialState("A"); 
		state.setInvariant("i < 10");
		state.setUrgent(true);
		state.setCommitted(true);
		
		Hta hta = xmlSerializer.generateModel(state);
		
		assertEquals("", nvl(hta.getDeclaration()));
		assertEquals(1, hta.getTemplate().size());
		
		Location l = hta.getTemplate().get(0).getLocation().get(0);
		assertTrue(l.getId().matches("Template.A.\\d+"));
		assertEquals("A", l.getName().getvalue());
		assertEquals("i < 10", findByKind(l.getLabel(), "invariant").getvalue());
		assertNotNull(l.getUrgent());
		assertNotNull(l.getCommitted());
		
		assertEquals("template := Template();", hta.getInstantiation());
		assertEquals("system template;", hta.getSystem());
		List<Globalinit> globalinits = hta.getGlobalinit();
		assertEquals(1, globalinits.size());
		assertEquals("template", globalinits.get(0).getInstantiationname());
		assertEquals("Template.ENTRY", ((Entry)globalinits.get(0).getRef()).getId());
	}

	@Test
	public void twoStatesAndAnEdge() throws Exception {
		State stateA = createInitialState("A");
		State stateB = createState("B");
		createEdge(stateA, stateB);
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		Template template = hta.getTemplate().get(0);
		
		List<Location> locations = template.getLocation();
		assertLocations(locations, Arrays.asList("A", "B"));
		
		List<Transition> transitions = template.getTransition();
		assertEquals(1, transitions.size());
		hasTransition(transitions, locations.get(0), locations.get(1));
	}
	
	@Test
	public void twoOutgoingEdges() throws Exception {
		State stateA = createInitialState("A");
		State stateB = createState("B");
		State stateC = createState("C");
		createEdge(stateA, stateB);
		createEdge(stateA, stateC);
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		Template template = hta.getTemplate().get(0);
		
		List<Location> locations = template.getLocation();
		assertEquals(3, locations.size());
		
		List<Transition> transitions = template.getTransition();
		assertEquals(2, transitions.size());
		assertTrue(hasTransition(transitions, locations.get(0), locations.get(1)));
		assertTrue(hasTransition(transitions, locations.get(0), locations.get(2)));
		assertNull(findByKind(transitions.get(0).getLabel(), "select"));
		assertNull(findByKind(transitions.get(0).getLabel(), "comments"));
	}
	
	@Test
	public void cyclic() throws Exception {
		State stateA = createInitialState("A");
		State stateB = createState("B");
		createEdge(stateA, stateB);
		createEdge(stateB, stateA);
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		Template template = hta.getTemplate().get(0);
		
		List<Location> locations = template.getLocation();
		assertEquals(2, locations.size());
		
		List<Transition> transitions = template.getTransition();
		assertEquals(2, transitions.size());
		assertTrue(hasTransition(transitions, locations.get(0), locations.get(1)));
		assertTrue(hasTransition(transitions, locations.get(1), locations.get(0)));
	}
	
	@Test
	public void transitionProperties_EverythingIsSet() throws Exception {
		State stateA = createInitialState("A");
		State stateB = createState("B");
		Edge edge = createEdge(stateA, stateB);
		edge.setGuard("a == 0");
		edge.setSelect("select");
		edge.setSync("s?");
		edge.setUpdate("a = 1");
		edge.setComments("comment");
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		Template template = hta.getTemplate().get(0);
		
		List<Location> locations = template.getLocation();
		Transition t = template.getTransition().get(0);
		
		assertEquals(locations.get(0), t.getSource().getRef());
		assertEquals(locations.get(1), t.getTarget().getRef());
		assertEquals("a == 0", findByKind(t.getLabel(), "guard").getvalue());
		assertEquals("select", findByKind(t.getLabel(), "select").getvalue());
		assertEquals("s?", findByKind(t.getLabel(), "synchronisation").getvalue());
		assertEquals("a = 1", findByKind(t.getLabel(), "assignment").getvalue());
		assertEquals("comment", findByKind(t.getLabel(), "comments").getvalue());
	}
	
	/**
	 * select and comments must be skipped when they are empty because 
	 * huppaal -> uppaal converter does not support them.
	 */
	@Test
	public void transitionProperties_NothingIsSet() throws Exception {
		State stateA = createInitialState("A");
		State stateB = createState("B");
		createEdge(stateA, stateB);
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		Template template = hta.getTemplate().get(0);
		
		Transition transition = template.getTransition().get(0);
		assertNull(findByKind(transition.getLabel(), "select"));
		assertNull(findByKind(transition.getLabel(), "comments"));
		assertEquals("", nvl(findByKind(transition.getLabel(), "assignment").getvalue()));
		assertEquals("", nvl(findByKind(transition.getLabel(), "synchronisation").getvalue()));
	}
	
	@Test
	public void withSubdiagram() throws Exception {
		State stateA = createInitialState("A");
		Diagram subDiagram = createDiagram("sub");
		Connector connector1 = createConnector(subDiagram, "con1");
		Connector connector2 = createConnector(subDiagram, "con2");
		State stateB = createState("B");
		State stateC = createState("C");
		
		createEdge(stateA, connector1);
		createEdge(connector1, stateC);
		createEdge(stateC, connector2);
		createEdge(connector2, stateB);
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		List<Template> templates = hta.getTemplate();
		assertEquals(2, templates.size());
		
		Template template = templates.get(0);
		List<Location> locations = template.getLocation();
		List<Transition> transitions = template.getTransition();
		
		Template subTemplate = templates.get(1);
		List<Location> subLocations = subTemplate.getLocation();
		List<Transition> subTransitions = subTemplate.getTransition();
		
		Component component = template.getComponent().get(0);
		assertEquals(subTemplate.getName().getvalue(), component.getInstantiates());
		assertTrue(component.getId().matches("Template.\\d+"));
		
		assertEquals(1, subTemplate.getEntry().size());
		assertEquals(1, subTemplate.getExit().size());
		assertLocations(locations, Arrays.asList("A", "B"));
		
		assertEquals(2, transitions.size());
		// ref - component id; entryref & exitref
		assertTrue(hasTransition(transitions, locations.get(0), subTemplate.getComponent().get(0)));
		assertTrue(hasTransition(transitions, subTemplate.getComponent().get(0), locations.get(1)));
		
		assertLocations(subLocations, Arrays.asList("C"));
		assertEquals(1, subTransitions.size());
		assertTrue(hasTransition(subTransitions, subLocations.get(0), subTemplate.getExit().get(0)));
		assertEquals(subTemplate.getLocation().get(0), subTemplate.getEntry().get(0).getConnection().get(0).getTarget().getRef());
		
		
	}
	
	private Label findByKind(Iterable<Label> labels, final String kind) {
		Iterator<Label> it = Iterables.filter(labels, new Predicate<Label>() {
			@Override
			public boolean apply(Label label) {
				return label.getKind().equals(kind);
			}
		}).iterator();
		
		if (it.hasNext()) {
			return it.next();
		}
		
		return null;
	}
	
	private boolean hasTransition(Iterable<Transition> transitions, final Object from, final Object to) {
		return Iterables.any(transitions, new Predicate<Transition>() {
			@Override
			public boolean apply(Transition t) {
				return t.getSource().getRef().equals(from) && t.getTarget().getRef().equals(to);
			}
		});
	}
	
	private void assertLocations(final Collection<Location> locations, final Collection<String> names) {
		assertEquals(names.size(), locations.size());
		
		assertTrue(Iterables.all(names, new Predicate<String>() {
			@Override
			public boolean apply(final String name) {
				return Iterables.any(locations, new Predicate<Location>() {
					@Override
					public boolean apply(Location location) {
						return location.getName().getvalue().equals(name);
					}
					
				});
			}
		}));
	}
	
	private State createInitialState(String value) {
		State state = createState(value);
		state.setInitial(true);
		return state;
	}

	private State createState(String value) {
		State state = EditorFactory.eINSTANCE.createState();
		state.setName(value);
		return state;
	}
	
	private Edge createEdge(EndPoint start, EndPoint end) {
		Edge edge = EditorFactory.eINSTANCE.createEdge();
		edge.setStart(start);
		edge.setEnd(end);
		return edge;
	}
	
	private Connector createConnector(Diagram diagram, String name) {
		Connector connector = EditorFactory.eINSTANCE.createConnector();
		connector.setName(name);
		connector.setDiagram(diagram);
		return connector;
	}
	
	private Diagram createDiagram(String name) {
		Diagram d = EditorFactory.eINSTANCE.createDiagram();
		d.setName(name);
		return d;
	}

}
