package org.botlaxy.telegramit.core.handler.dsl

import org.botlaxy.telegramit.core.handler.CommandParser
import org.botlaxy.telegramit.core.handler.DefaultCommandParser
import org.botlaxy.telegramit.core.handler.HandlerException

@DslMarker
annotation class HandlerDsl

fun handler(vararg commands: String, body: HandlerBuilder.() -> Unit): Handler {
    val handlerBuilder = HandlerBuilder(commands.asList())
    return handlerBuilder.build(body)
}

@HandlerDsl
class HandlerBuilder(private val commands: List<String>) {

    private val stepBuilders: MutableList<StepBuilder<*>> = arrayListOf()
    private var process: ProcessBlock? = null
    private var commandParser: CommandParser = DefaultCommandParser()

    fun <T : Any> step(key: String, block: StepBuilder<T>.() -> Unit): StepBuilder<T> {
        val stepBuilder: StepBuilder<T> = StepBuilder<T>(key).apply(block)
        stepBuilders.add(stepBuilder)

        return stepBuilder
    }

    fun process(processor: ProcessBlock) {
        this.process = processor
    }

    internal fun build(body: HandlerBuilder.() -> Unit): Handler {
        body()
        val steps = arrayListOf<Step<*>>()
        for ((index, stepBuilder) in stepBuilders.withIndex()) {
            val step = stepBuilder.build {
                // Next step
                if (index + 1 < stepBuilders.size) {
                    stepBuilders[index + 1].key
                } else {
                    null
                }
            }
            steps.add(step)
        }
        val handlerCommands = commands.map { cmd -> commandParser.parse(cmd) }

        return Handler(
            handlerCommands,
            steps.associateBy { it.key },
            process ?: throw HandlerException("Process block must not be null")
        )
    }

}

@HandlerDsl
class StepBuilder<T : Any>(val key: String) {

    private var entry: EntryBlock? = null
    private var validation: ValidationBlock? = null
    private var resolver: ResolverBlock<T>? = null
    private var next: NextStepBlock? = null

    fun entry(entry: EntryBlock) {
        this.entry = entry
    }

    fun validation(validation: ValidationBlock) {
        this.validation = validation
    }

    fun resolver(resolver: ResolverBlock<T>) {
        this.resolver = resolver
    }

    fun next(next: NextStepBlock) {
        this.next = next
    }

    internal fun build(defaultNext: NextStepBlock): Step<T> {
        return Step(
            key,
            entry ?: throw HandlerException("Step 'entry' block can't be null"),
            validation,
            resolver,
            next ?: defaultNext
        )
    }

}
