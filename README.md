# SwipeStack
A simple and easy to use swipeable view stack for Android.

![Demo screen 1](https://raw.githubusercontent.com/flschweiger/SwipeStack/master/art/screen1.png)
![Demo animation](https://raw.githubusercontent.com/flschweiger/SwipeStack/master/art/demo.gif)
![Demo screen 2](https://raw.githubusercontent.com/flschweiger/SwipeStack/master/art/screen2.png)  

## How to use it ##
### Include the Gradle dependency ###

```java
dependencies {
    compile 'link.fls:swipestack:0.1.0'
}
```

### Use it in your layout file ###
1. Use the `link.fls.swipestack` view in your XML layout file 
2. Set the parent view's `clipChildren` attribute to `false`

*Example:*

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clipChildren="false">

    <link.fls.swipestack
        android:id="@+id/swipeStack"
        android:layout_width="match_parent"
        android:layout_height="@dimen/swipe_stack_height"
        android:padding="@dimen/padding_swipestack"
        app:stack_rotation="10"
        app:stack_size="3"/>

</FrameLayout>
```

### That's it! ###

## Attributes ##
*All attributes are optional.*

`animation_duration` specifies the duration of the animations. *Default: 300ms*

`stack_size` specifies the maximum number of visible views. *Default: 3*

`stack_spacing` specifies the vertical distance between two views. *Default: 10px*

`stack_rotation` specifies the maximum random ratation (in degrees) of a card on the stack. *Default: 8*

`swipe_rotation` specifies the rotation (in degrees) of the view when it gets swiped left / right. *Default: 15*

`swipe_opacity` specifies the opacity of the view when it gets swiped left / right. *Default: 1.0*

`disable_hw_acceleration` set to `true` disables hardware acceleration. *Default: false*

## Copyright Notice ##
``` 
Copyright (C) 2016 Frederik Schweiger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ```
