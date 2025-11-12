import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { FieldGroup, StandardField, CustomField } from '@/types'

export const useFieldStore = defineStore('field', () => {
  const fieldGroups = ref<FieldGroup[]>([])
  const standardFields = ref<StandardField[]>([])
  const customFields = ref<CustomField[]>([])

  const setFieldGroups = (groups: FieldGroup[]) => {
    fieldGroups.value = groups
  }

  const setStandardFields = (fields: StandardField[]) => {
    standardFields.value = fields
  }

  const setCustomFields = (fields: CustomField[]) => {
    customFields.value = fields
  }

  return {
    fieldGroups,
    standardFields,
    customFields,
    setFieldGroups,
    setStandardFields,
    setCustomFields,
  }
})

