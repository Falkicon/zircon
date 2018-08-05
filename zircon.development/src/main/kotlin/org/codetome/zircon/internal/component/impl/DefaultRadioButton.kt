package org.codetome.zircon.internal.component.impl

import org.codetome.zircon.api.builder.component.ComponentStyleSetBuilder
import org.codetome.zircon.api.builder.graphics.StyleSetBuilder
import org.codetome.zircon.api.color.TextColor
import org.codetome.zircon.api.component.ColorTheme
import org.codetome.zircon.api.component.ComponentStyleSet
import org.codetome.zircon.api.component.RadioButton
import org.codetome.zircon.api.data.Position
import org.codetome.zircon.api.data.Size
import org.codetome.zircon.api.data.Tile
import org.codetome.zircon.api.input.Input
import org.codetome.zircon.api.resource.TilesetResource
import org.codetome.zircon.api.util.Maybe
import org.codetome.zircon.internal.component.WrappingStrategy
import org.codetome.zircon.internal.component.impl.DefaultRadioButton.RadioButtonState.*
import org.codetome.zircon.internal.util.ThreadSafeQueue

class DefaultRadioButton(private val text: String,
                         wrappers: ThreadSafeQueue<WrappingStrategy>,
                         width: Int,
                         initialTileset: TilesetResource<out Tile>,
                         position: Position,
                         componentStyleSet: ComponentStyleSet)
    : RadioButton, DefaultComponent(
        size = Size.create(width, 1),
        position = position,
        componentStyleSet = componentStyleSet,
        wrappers = wrappers,
        tileset = initialTileset) {

    private val maxTextLength = width - BUTTON_WIDTH - 1
    private val clearedText = if (text.length > maxTextLength) {
        text.substring(0, maxTextLength - 3).plus("...")
    } else {
        text
    }

    private var state = NOT_SELECTED

    init {
        redrawContent()
    }

    private fun redrawContent() {
        getDrawSurface().putText("${STATES[state]} $clearedText")
    }

    override fun isSelected() = state == SELECTED

    fun select() {
        if (state != SELECTED) {
            getDrawSurface().applyStyle(getComponentStyles().mouseOver())
            state = SELECTED
            redrawContent()
        }
    }

    fun removeSelection() =
            if (state != NOT_SELECTED) {
                getDrawSurface().applyStyle(getComponentStyles().reset())
                state = NOT_SELECTED
                redrawContent()
                true
            } else {
                false
            }

    override fun acceptsFocus(): Boolean {
        return true
    }

    override fun giveFocus(input: Maybe<Input>): Boolean {
        getDrawSurface().applyStyle(getComponentStyles().giveFocus())
        return true
    }

    override fun takeFocus(input: Maybe<Input>) {
        getDrawSurface().applyStyle(getComponentStyles().reset())
    }

    override fun getText() = text

    override fun applyColorTheme(colorTheme: ColorTheme) {
        setComponentStyles(ComponentStyleSetBuilder.newBuilder()
                .defaultStyle(StyleSetBuilder.newBuilder()
                        .foregroundColor(colorTheme.getAccentColor())
                        .backgroundColor(TextColor.transparent())
                        .build())
                .mouseOverStyle(StyleSetBuilder.newBuilder()
                        .foregroundColor(colorTheme.getBrightBackgroundColor())
                        .backgroundColor(colorTheme.getAccentColor())
                        .build())
                .focusedStyle(StyleSetBuilder.newBuilder()
                        .foregroundColor(colorTheme.getDarkBackgroundColor())
                        .backgroundColor(colorTheme.getAccentColor())
                        .build())
                .activeStyle(StyleSetBuilder.newBuilder()
                        .foregroundColor(colorTheme.getDarkForegroundColor())
                        .backgroundColor(colorTheme.getAccentColor())
                        .build())
                .build())
    }

    enum class RadioButtonState {
        PRESSED,
        SELECTED,
        NOT_SELECTED
    }

    companion object {

        private val PRESSED_BUTTON = "<o>" // TODO: not used now
        private val SELECTED_BUTTON = "<O>"
        private val NOT_SELECTED_BUTTON = "< >"

        private val BUTTON_WIDTH = NOT_SELECTED_BUTTON.length

        private val STATES = mapOf(
                Pair(PRESSED, PRESSED_BUTTON),
                Pair(SELECTED, SELECTED_BUTTON),
                Pair(NOT_SELECTED, NOT_SELECTED_BUTTON))

    }
}
