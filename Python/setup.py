from setuptools import setup, find_packages

setup(
    name='Python-Demo',
    version='1.1.1',
    packages=find_packages(),
    install_requires=[
        'requests',
        'PyYAML',
        'Pillow'
    ],
    entry_points={
        'console_scripts': [
            'run_python_demo=apps.RemoteInteractionDemo:main',
        ],
    },
)