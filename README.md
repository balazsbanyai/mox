# CreateMox

An Intellij plugin that adds a quick fix to create mock objects in test code. 

Use wisely as constructors with a lot of parameters may be evil.

*Attention*
Requires the target project to compile Mockito as the @Mock annotation are currently used from that package.

# Usage

1. Create new test
2. Type the constructor of the class under test
![Before](https://raw.githubusercontent.com/balazsbanyai/mox/documentation/before.png)

3. Without filling the parameters, open the Quick Fix context menu and select "Create mocks"
![After](https://raw.githubusercontent.com/balazsbanyai/mox/documentation/after.png)
