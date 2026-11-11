package me.chubbyduck.canvas.elements;

import java.util.List;
import me.chubbyduck.canvas.elements.control.ArrowButton;
import me.chubbyduck.canvas.elements.control.Button;
import me.chubbyduck.canvas.elements.control.DropdownList;
import me.chubbyduck.canvas.elements.control.DropdownNode;
import me.chubbyduck.canvas.elements.control.Pager;
import me.chubbyduck.canvas.elements.control.Segmented;
import me.chubbyduck.canvas.elements.control.Slider;
import me.chubbyduck.canvas.elements.control.SliderTrack;
import me.chubbyduck.canvas.elements.control.Stepper;
import me.chubbyduck.canvas.elements.control.Tabs;
import me.chubbyduck.canvas.elements.control.Toggle;
import me.chubbyduck.canvas.elements.display.Divider;
import me.chubbyduck.canvas.elements.display.Icon;
import me.chubbyduck.canvas.elements.display.Panel;
import me.chubbyduck.canvas.elements.display.SettingRow;
import me.chubbyduck.canvas.elements.display.Spacer;
import me.chubbyduck.canvas.elements.display.Text;
import me.chubbyduck.canvas.elements.input.ActionBar;
import me.chubbyduck.canvas.elements.input.NativeInput;
import me.chubbyduck.canvas.elements.layout.Absolute;
import me.chubbyduck.canvas.elements.layout.Flex;
import me.chubbyduck.canvas.elements.layout.Stack;
import me.chubbyduck.canvas.node.Node;
import org.jspecify.annotations.NullMarked;

/** The built-in node classes, for {@code CanvasRuntime.bootstrap(plugin).register(Nodes.defaults())}. */
@NullMarked
public final class Nodes {

    private Nodes() {
    }

    public static List<Class<? extends Node>> defaults() {
        return List.of(
            Absolute.class,
            Flex.class,
            Stack.class,
            Text.class,
            Icon.class,
            Spacer.class,
            Divider.class,
            Panel.class,
            Button.class,
            ArrowButton.class,
            Segmented.class,
            Tabs.class,
            Stepper.class,
            SliderTrack.class,
            Slider.class,
            Toggle.class,
            NativeInput.class,
            DropdownNode.class,
            DropdownList.class,
            ActionBar.class,
            SettingRow.class,
            Pager.class
        );
    }
}
