<h1>Game Engine</h1>
<p>A simple game engine implemented in Java using LWJGL 3</p>

<p>The engine currently supports:
<ul>
<li>Scripting in JavaScript through the JavaScript Nashorn engine</li>
<li>Interface with opengl and input events using GLFW</li>
<li>Rendering of 3D models loaded from a custom JSON format with additional binary data
    <ul>
    <li>Conversion from Wavefront obj and Collada dae files to the custom format</li>
    <li>Support for multiple meshes with individual shaders and materials and multiple textures</li>
    <li>Support for arbitrary vertex attributes and shaders</li>
    <li>Support for arbitrary uniform data and buffers</li>
    <li>No support for animation yet</li>
    </ul></li>
<li>Text display using fonts loaded from TTF files</li>
<li>Event handling and generation using dispatches</li>
<li>Resource management for resource reuse and queueing resources for loading in a seperate thread</li>
<li>Multi-threading for graphics, resource loading, game logic updates</li>
</ul></p>

<hr />
<h3>Libraries</h3>
<p>JSON is parsed using the org.json library</p>
<p>Math is computed with the JOML Java Open Math Library</p>


<hr />
<p>This project is the spiritual successor to the previous Voyager Engine</p>
