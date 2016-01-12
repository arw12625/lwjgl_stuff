Game Engine

A simple game engine implemented in Java using LWJGL 3
The engine currently supports:
-- Scripting in javascript through the Javscript Nashorn engine
-- Interface with opengl and input events using GLFW
-- Rendering of 3D models loaded from a custom JSON format with additional binary data
    -- Conversion from Wavefront obj and Collada dae files to the custom format
    -- Support for multiple meshes with individual shaders and materials and multiple textures
    -- Support for arbitrary vertex attributes and shaders
    -- Support for arbitrary uniform data and buffers
    -- No support for animation yet
-- Text display using fonts loaded from TTF files
-- Event handling and generation using dispatches
-- Resource management for resource reuse and queueing resources for loading in a seperate thread
-- Multithreading for graphics, resource loading, game logic updates

JSON is parsed using the org.json library
Math is computed with the JOML Java Open Math Library


This project is the spiritual successor to the previous Voyager Engine
