import { describe, it, expect } from 'vitest'
import { useWorkflow } from '../composables/useWorkflow'

describe('useWorkflow', () => {
  const { getAvailableTransitions } = useWorkflow()

  it('DRAFT can transition to RECEIVING', () => {
    const transitions = getAvailableTransitions('DRAFT')
    const targets = transitions.map(t => t.targetState)
    expect(targets).toContain('RECEIVING')
  })

  it('RECEIVING can transition to INSPECTION', () => {
    const transitions = getAvailableTransitions('RECEIVING')
    const targets = transitions.map(t => t.targetState)
    expect(targets).toContain('INSPECTION')
  })

  it('INSPECTION can transition to PUTAWAY', () => {
    const transitions = getAvailableTransitions('INSPECTION')
    const targets = transitions.map(t => t.targetState)
    expect(targets).toContain('PUTAWAY')
  })

  it('PUTAWAY can transition to COMPLETED', () => {
    const transitions = getAvailableTransitions('PUTAWAY')
    const targets = transitions.map(t => t.targetState)
    expect(targets).toContain('COMPLETED')
  })

  it('any state can transition to REJECTED', () => {
    const draftTransitions = getAvailableTransitions('DRAFT')
    expect(draftTransitions.map(t => t.targetState)).toContain('REJECTED')

    const receivingTransitions = getAvailableTransitions('RECEIVING')
    expect(receivingTransitions.map(t => t.targetState)).toContain('REJECTED')

    const inspectionTransitions = getAvailableTransitions('INSPECTION')
    expect(inspectionTransitions.map(t => t.targetState)).toContain('REJECTED')
  })

  it('DRAFT cannot skip to COMPLETED', () => {
    const transitions = getAvailableTransitions('DRAFT')
    const targets = transitions.map(t => t.targetState)
    expect(targets).not.toContain('COMPLETED')
  })
})
