/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2017 the original author or authors.
 */
package org.assertj.assertions.generator.description.converter;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.BeanWithExceptionsTest;
import org.assertj.assertions.generator.NestedClassesTest;
import org.assertj.assertions.generator.data.art.ArtWork;
import org.assertj.assertions.generator.data.Movie;
import org.assertj.assertions.generator.data.Team;
import org.assertj.assertions.generator.data.TreeEnum;
import org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.PlayerAgent;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.util.ClassUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class ClassToClassDescriptionConverterTest implements NestedClassesTest, BeanWithExceptionsTest {
	private static ClassToClassDescriptionConverter converter;

	@BeforeClass
	public static void beforeAllTests() {
		converter = new ClassToClassDescriptionConverter();
	}

	@Test
	public void should_build_player_class_description() throws Exception {
		// Given
		Class<?> clazz = Player.class;

		ClassDescription classDescription = converter.convertToClassDescription(clazz);
		assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo(clazz.getSimpleName());
		assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data.nba");

		assertThat(classDescription.getGettersDescriptions()).hasSize(19);
	}

	@Test
	public void should_build_movie_class_description() throws Exception {
		// Given
		Class<?> clazz = Movie.class;

		ClassDescription classDescription = converter.convertToClassDescription(clazz);
		assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo(clazz.getSimpleName());
		assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");

		assertThat(classDescription.getGettersDescriptions()).hasSize(3);
		assertThat(classDescription.getFieldsDescriptions()).hasSize(4);
		assertThat(classDescription.getDeclaredGettersDescriptions()).hasSize(2);
		assertThat(classDescription.getDeclaredFieldsDescriptions()).hasSize(3);
		assertThat(classDescription.getSuperType()).isEqualTo(TypeToken.of(ArtWork.class));
	}

	@Theory
	public void should_build_nestedclass_description(NestedClass nestedClass) throws Exception {
		Class<?> clazz = nestedClass.getNestedClass();
		ClassDescription classDescription = converter.convertToClassDescription(clazz);
		assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo(nestedClass.getClassNameWithOuterClass());
		assertThat(classDescription.getPackageName()).isEqualTo(clazz.getPackage().getName());
		assertThat(classDescription.getGettersDescriptions()).hasSize(1);
	}

	@Theory
	public void should_build_getter_with_exception_description(GetterWithException getter) throws Exception {
		TypeToken<?> type = getter.getBeanClass();
		Class<?> clazz = type.getRawType();
		ClassDescription classDescription = converter.convertToClassDescription(clazz);
		assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo(clazz.getSimpleName());
		assertThat(classDescription.getPackageName()).isEqualTo(clazz.getPackage().getName());
		assertThat(classDescription.getGettersDescriptions()).hasSize(4);

		for (GetterDescription desc : classDescription.getGettersDescriptions()) {
			if (desc.getName().equals(getter.getPropertyName())) {
				assertThat(desc.getExceptions()).containsOnly(getter.getExceptions().toArray(new TypeToken[]{}));
				break;
			}
		}
	}

	class WithPrimitiveArrayCollection {
		List<int[]> scores;

		@SuppressWarnings("unused")
		public List<int[]> getScores() {
			return scores;
		}
	}

	@Test
	public void should_build_class_description_for_iterable_of_primitive_type_array() throws Exception {
		// Given
		Class<?> clazz = WithPrimitiveArrayCollection.class;

		// When
		ClassDescription classDescription = converter.convertToClassDescription(clazz);
		assertThat(classDescription.getGettersDescriptions()).hasSize(1);

		// Then
		GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
		assertThat(getterDescription.isIterableType())
				.as("getterDescription must be iterable")
				.isTrue();
		assertThat(getterDescription.getElementTypeName(clazz.getPackage().getName()))
				.as("getterDesc must have correct element type")
				.isEqualTo("int[]");
		assertThat(getterDescription.isArrayType())
				.as("getterDescription must not be an array")
				.isFalse();
	}

	static class WithPrimitiveArrayArrayCollection {
		int[][] scores;

		@SuppressWarnings("unused")
		public int[][] getScores() {
			return scores;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_to_build_class_description_for_local_class() throws Exception {
		class Local {}
		converter.convertToClassDescription(Local.class);
	}

	@Test
	public void should_build_class_description_for_array_of_primitive_type_array() throws Exception {
		ClassDescription classDescription = converter.convertToClassDescription(WithPrimitiveArrayArrayCollection.class);
		assertThat(classDescription.getGettersDescriptions()).hasSize(1);
		GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
		assertThat(getterDescription.isIterableType()).as("getterDescription is an iterable ?").isFalse();
		assertThat(getterDescription.isArrayType()).as("getterDescription is an array ?").isTrue();
		assertThat(getterDescription.getElementTypeName(WithPrimitiveArrayArrayCollection.class.getPackage().getName())).isEqualTo("int[]");
	}

	@Test
	public void should_build_class_description_for_enum_type() throws Exception {
		ClassDescription classDescription = converter.convertToClassDescription(TreeEnum.class);
		// should not contain getDeclaringClassGetter as we don't want to have hasDeclaringClass assertion
		assertThat(classDescription.getGettersDescriptions()).hasSize(1);
		GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
		assertThat(getterDescription.isIterableType()).as("getterDescription must be iterable").isTrue();
		assertThat(getterDescription.getElementTypeName(TreeEnum.class.getPackage().getName()))
				.as("getterDescription must get the internal component type without package")
				.isEqualTo(TreeEnum.class.getSimpleName());
		assertThat(getterDescription.isArrayType()).as("getterDescription must be an array").isFalse();
	}

	class WithIterableObjectType {
		List<Player[]> players;

		@SuppressWarnings("unused")
		public List<Player[]> getPlayers() {
			return players;
		}
	}

	@Test
	public void should_build_class_description_for_iterable_of_Object_type() throws Exception {
		// Given
		Class<?> clazz = WithIterableObjectType.class;

		// When
		ClassDescription classDescription = converter.convertToClassDescription(clazz);

		// Then
		assertThat(classDescription.getGettersDescriptions()).hasSize(1);
		GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
		assertThat(getterDescription.isIterableType())
				.as("getterDescription must be iterable")
				.isTrue();
		assertThat(getterDescription.getElementTypeName(WithIterableObjectType.class.getPackage().getName()))
				.as("getterDesc element type must return correct array type")
				.isEqualTo(ClassUtil.getTypeDeclaration(new TypeToken<Player[]>() {}, false, false));
		assertThat(getterDescription.isArrayType()).as("getterDescription is not an array").isFalse();
	}

	@Test
	public void should_build_class_description_for_interface() throws Exception {
		// Given an interface
		Class<?> clazz = PlayerAgent.class;
		// When
		ClassDescription classDescription = converter.convertToClassDescription(clazz);

		// Then
		assertThat(classDescription.getSuperType()).isNull();
		assertThat(classDescription.getGettersDescriptions()).hasSize(1);
		GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
		assertThat(getterDescription.isIterableType())
				.as("getterDescription is not iterable").isFalse();
		assertThat(getterDescription.getName())
				.as("getterDesc must have correct name").isEqualTo("managedPlayer");
		assertThat(getterDescription.getTypeName(false, false))
				.as("getterDesc must have correct owning type").isEqualTo(Player.class.getSimpleName());
		assertThat(getterDescription.getTypeName(true, false))
				.as("getterDesc must have correct owning type").isEqualTo(Player.class.getName());
	}

	@Test
	public void should_build_fellowshipOfTheRing_class_description() throws Exception {
		// Given
		Class<?> clazz = FellowshipOfTheRing.class;

		// Then
		ClassDescription classDescription = converter.convertToClassDescription(clazz);
		assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("FellowshipOfTheRing");
		assertThat(classDescription.getClassNameWithOuterClassNotSeparatedByDots()).isEqualTo("FellowshipOfTheRing");
		assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data.lotr");
		assertThat(classDescription.getGettersDescriptions()).hasSize(1);
	}

	@Test
	public void should_handle_toString() {
		ClassDescription classDescription = converter.convertToClassDescription(FellowshipOfTheRing.class);
		assertThat(classDescription.toString()).contains(FellowshipOfTheRing.class.getName());
	}

	@Test
	public void should_build_class_description_for_class_with_public_fields() throws Exception {
		// Given
		Class<?> clazz = Team.class;

		// Then
		ClassDescription classDescription = converter.convertToClassDescription(clazz);
		assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("Team");
		assertThat(classDescription.getClassNameWithOuterClassNotSeparatedByDots()).isEqualTo("Team");
		assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
		assertThat(classDescription.getGettersDescriptions()).extracting("name").containsExactly("division");
		assertThat(classDescription.getFieldsDescriptions()).extracting("name").containsOnly("name",
				"oldNames",
				"westCoast",
				"rank",
				"players",
				"points",
				"victoryRatio");
	}

	class Bug21_SQLException extends SQLException {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		public SQLException getExceptionChain() {
			return null;
		}
	}

	@Test
	public void bug21_reflection_error_on_iterable_ParameterizedType() {
		ClassDescription classDescription = converter.convertToClassDescription(Bug21_SQLException.class);
		// exceptionChain is a SQLException which is an Iterable<Throwable> but looking only at SQLException we can't deduce
		// iterable valueType
		assertThat(classDescription.getGettersDescriptions()).extracting("name").contains("exceptionChain");
	}

	@Test
	public void should_only_describe_overriden_getter_once() {
		ClassDescription myClassDescription = converter.convertToClassDescription(ClassOverridingGetter.class);
		assertThat(myClassDescription.getGettersDescriptions()).extracting("name").containsOnlyOnce("myList");
	}

	public interface InterfaceWithGetter {
		List<String> getMyList();
	}

	class ClassOverridingGetter implements InterfaceWithGetter {
		@Override
		public ArrayList<String> getMyList() {
			return null;
		}
	}

}
