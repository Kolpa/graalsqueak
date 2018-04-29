import os
import argparse

import mx
import mx_gate
import mx_unittest


PACKAGE_NAME = 'de.hpi.swa.graal.squeak'

_suite = mx.suite('graalsqueak')
_compiler = mx.suite('compiler', fatalIfMissing=False)


def _graal_vm_args(args, jdk):
    graal_args = []

    if args.trace_compilation:
        graal_args += [
            '-Dgraal.TraceTruffleCompilation=true',
        ]

    if args.perf_warnings:
        graal_args += [
            '-Dgraal.TruffleCompilationExceptionsAreFatal=true',
            '-Dgraal.TraceTrufflePerformanceWarnings=true',
            '-Dgraal.TraceTruffleCompilationDetails=true',
            '-Dgraal.TraceTruffleExpansionSource=true']

    if args.trace_invalidation:
        graal_args += [
            '-Dgraal.TraceTruffleTransferToInterpreter=true',
            '-Dgraal.TraceTruffleAssumptions=true',
        ]

    if args.igv:
        print 'Sending Graal dumps to igv...'
        graal_args += [
            '-Dgraal.Dump=Metaclass,Truffle,hpi',
        ]

    if args.low_level:
        graal_args += [
            '-XX:+UnlockDiagnosticVMOptions',
            '-XX:+LogCompilation',
            '-XX:+TraceDeoptimization',
        ]

    if args.print_machine_code:
        graal_args += [
            '-XX:CompileCommand=print,*OptimizedCallTarget.callRoot',
            '-XX:CompileCommand=exclude,*OptimizedCallTarget.callRoot',
        ]

    if not args.background_compilation:
        graal_args += ['-Dgraal.TruffleBackgroundCompilation=false']

    graal_args += [
        '-Djvmci.Compiler=graal',
        '-XX:+UseJVMCICompiler',
    ]
    return graal_args


def _squeak(args, extra_vm_args=None, env=None, jdk=None, **kwargs):
    env = env if env else os.environ

    vm_args, raw_args = mx.extract_VM_args(
        args, useDoubleDash=True, defaultAllVMArgs=False)

    parser = argparse.ArgumentParser(prog='mx squeak')
    parser.add_argument('-A', '--assertions',
                        help='enable assertion',
                        dest='assertions',
                        action='store_true', default=False)
    parser.add_argument('-B', '--no-background',
                        help='disable background compilation',
                        dest='background_compilation',
                        action='store_false', default=True)
    parser.add_argument('--igv', action='store_true', help='dump to igv')
    parser.add_argument('-l', '--low-level',
                        help='enable low-level optimization output',
                        dest='low_level', action='store_true', default=False)
    parser.add_argument('--machine-code',
                        help='print machine code',
                        dest='print_machine_code', action='store_true',
                        default=False)
    parser.add_argument(
        '-t', '--trace-compilation',
        help='trace truffle compilation',
        dest='trace_compilation', action='store_true', default=False)
    parser.add_argument(
        '-ti', '--trace-invalid',
        help='trace assumption invalidation and transfers to interpreter',
        dest='trace_invalidation', action='store_true', default=False)
    parser.add_argument('-v', '--verbose',
                        help='enable verbose output',
                        dest='verbose',
                        action='store_true', default=False)
    parser.add_argument('-w', '--perf-warnings',
                        help='enable performance warnings',
                        dest='perf_warnings',
                        action='store_true', default=False)
    parser.add_argument('squeak_args', nargs=argparse.REMAINDER)
    parsed_args = parser.parse_args(raw_args)

    vm_args = ['-cp', mx.classpath(PACKAGE_NAME)]

    if not jdk:
        jdk = mx.get_jdk(tag='jvmci')

    if _compiler:
        vm_args.extend(_graal_vm_args(parsed_args, jdk))

    # default: assertion checking is enabled
    if parsed_args.assertions:
        vm_args.extend(['-ea', '-esa'])

    if extra_vm_args:
        vm_args.extend(extra_vm_args)

    vm_args.append('de.hpi.swa.graal.squeak.GraalSqueakMain')
    return mx.run_java(vm_args + parsed_args.squeak_args, jdk=jdk, **kwargs)


def _graalsqueak_gate_runner(args, tasks):
    os.environ['MX_GATE'] = 'true'
    unittest_args = _get_jacoco_agent_args()
    unittest_args.extend(['--suite', 'graalsqueak'])
    with mx_gate.Task('TestGraalSqueak', tasks, tags=['test']) as t:
        if t:
            mx_unittest.unittest(unittest_args)
    with mx_gate.Task('CodeCoverageReport', tasks, tags=['test']) as t:
        if t:
            mx.command_function('jacocoreport')(['--format', 'xml', '.'])


def _get_jacoco_agent_args():
    jacocoagent = mx.library('JACOCOAGENT', True)

    includes = []
    baseExcludes = []
    for p in mx.projects(limit_to_primary=True):
        projsetting = getattr(p, 'jacoco', '')
        if projsetting == 'exclude':
            baseExcludes.append(p.name)
        if projsetting == 'include':
            includes.append(p.name + '.*')

    excludes = [package + '.*' for package in baseExcludes]
    agentOptions = {
                    'append': 'false',
                    'inclbootstrapclasses': 'false',
                    'includes': ':'.join(includes),
                    'excludes': ':'.join(excludes),
                    'destfile': 'jacoco.exec'
    }
    return ['-javaagent:' + jacocoagent.get_path(True) + '=' +
            ','.join([k + '=' + v for k, v in agentOptions.items()])]


mx.update_commands(_suite, {
    'squeak': [_squeak, '[Squeak args|@VM options]'],
})
mx_gate.add_gate_runner(_suite, _graalsqueak_gate_runner)
